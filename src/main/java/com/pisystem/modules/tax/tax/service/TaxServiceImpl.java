package com.tax.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.security.AuthenticationHelper;
import com.tax.data.*;
import com.tax.repo.*;

@Service
public class TaxServiceImpl implements TaxService {

    private final TaxRepository repository;
    private final AuthenticationHelper authenticationHelper;
    private final CapitalGainsRepository capitalGainsRepository;
    private final TaxSavingRepository taxSavingRepository;
    private final TDSRepository tdsRepository;

    public TaxServiceImpl(
            final TaxRepository repository,
            final AuthenticationHelper authenticationHelper,
            final CapitalGainsRepository capitalGainsRepository,
            final TaxSavingRepository taxSavingRepository,
            final TDSRepository tdsRepository) {
        this.repository = repository;
        this.authenticationHelper = authenticationHelper;
        this.capitalGainsRepository = capitalGainsRepository;
        this.taxSavingRepository = taxSavingRepository;
        this.tdsRepository = tdsRepository;
    }

    @Override
    @Transactional
    public TaxDTO createTaxDetails(Tax tax) {
        authenticationHelper.validateUserAccess(tax.getUserId());
        // Calculate net tax liability
        BigDecimal totalTax = calculateTotalTax(tax);
        tax.setTaxPayable(totalTax);
        if (tax.getCreatedDate() == null) {
            tax.setCreatedDate(LocalDate.now());
        }
        tax.setUpdatedDate(LocalDate.now());
        Tax saved = this.repository.save(tax);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxDTO getTaxDetailsByUserId(Long userId, String financialYear) {
        authenticationHelper.validateUserAccess(userId);
        List<Tax> taxes = this.repository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("financialYear"), financialYear)));

        return taxes.isEmpty() ? null : mapToDTO(taxes.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalTaxLiability(Long userId) {
        List<Tax> taxes = this.repository.findAll((root, query, cb) -> cb.equal(root.get("userId"), userId));

        return taxes.stream()
                .map(Tax::getTaxPayable)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getOutstandingTaxLiability(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        List<Tax> taxes = this.repository.findAll((root, query, cb) -> cb.equal(root.get("userId"), userId));

        return taxes.stream()
                .map(tax -> {
                    BigDecimal payable = tax.getTaxPayable() != null ? tax.getTaxPayable() : BigDecimal.ZERO;
                    BigDecimal paid = tax.getTaxPaid() != null ? tax.getTaxPaid() : BigDecimal.ZERO;
                    return payable.subtract(paid);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxRegimeComparisonDTO compareTaxRegimes(Long userId, String financialYear, BigDecimal grossIncome) {
        authenticationHelper.validateUserAccess(userId);
        
        // Get existing tax details
        List<Tax> taxes = this.repository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("financialYear"), financialYear)));
        
        Tax tax = taxes.isEmpty() ? Tax.builder().userId(userId).financialYear(financialYear).build() : taxes.get(0);
        
        // Get tax saving investments
        List<TaxSavingInvestment> investments = taxSavingRepository.findByUserIdAndFinancialYear(userId, financialYear);
        BigDecimal total80C = investments.stream()
                .filter(inv -> "80C".equals(inv.getSection()))
                .map(TaxSavingInvestment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total80D = investments.stream()
                .filter(inv -> "80D".equals(inv.getSection()))
                .map(TaxSavingInvestment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate Old Regime Tax
        BigDecimal standardDeduction = new BigDecimal("50000");
        BigDecimal oldRegimeDeductions = total80C.min(new BigDecimal("150000"))
                .add(total80D.min(new BigDecimal("25000")))
                .add(standardDeduction);
        BigDecimal oldRegimeTaxableIncome = grossIncome.subtract(oldRegimeDeductions).max(BigDecimal.ZERO);
        BigDecimal oldRegimeTax = calculateOldRegimeTax(oldRegimeTaxableIncome);
        BigDecimal oldRegimeTotalTax = oldRegimeTax.add(oldRegimeTax.multiply(new BigDecimal("0.04"))); // 4% cess
        
        // Calculate New Regime Tax
        BigDecimal newRegimeDeductions = standardDeduction; // Only standard deduction allowed
        BigDecimal newRegimeTaxableIncome = grossIncome.subtract(newRegimeDeductions).max(BigDecimal.ZERO);
        BigDecimal newRegimeTax = calculateNewRegimeTax(newRegimeTaxableIncome);
        BigDecimal newRegimeTotalTax = newRegimeTax.add(newRegimeTax.multiply(new BigDecimal("0.04"))); // 4% cess
        
        // Comparison
        BigDecimal taxSavings = oldRegimeTotalTax.subtract(newRegimeTotalTax);
        TaxRegime recommended = taxSavings.compareTo(BigDecimal.ZERO) > 0 ? TaxRegime.OLD : TaxRegime.NEW;
        String recommendation = taxSavings.compareTo(BigDecimal.ZERO) > 0 
                ? "Old regime saves you ₹" + taxSavings.abs().setScale(0, RoundingMode.HALF_UP) 
                : "New regime saves you ₹" + taxSavings.abs().setScale(0, RoundingMode.HALF_UP);
        
        return TaxRegimeComparisonDTO.builder()
                .userId(userId)
                .financialYear(financialYear)
                .grossIncome(grossIncome)
                .oldRegimeTaxableIncome(oldRegimeTaxableIncome)
                .oldRegimeDeductions(oldRegimeDeductions)
                .oldRegimeTotalTax(oldRegimeTotalTax)
                .oldRegimeEffectiveRate(grossIncome.compareTo(BigDecimal.ZERO) > 0 
                        ? oldRegimeTotalTax.divide(grossIncome, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO)
                .oldRegimeTaxSlabs(getOldRegimeTaxSlabs(oldRegimeTaxableIncome))
                .newRegimeTaxableIncome(newRegimeTaxableIncome)
                .newRegimeDeductions(newRegimeDeductions)
                .newRegimeTotalTax(newRegimeTotalTax)
                .newRegimeEffectiveRate(grossIncome.compareTo(BigDecimal.ZERO) > 0
                        ? newRegimeTotalTax.divide(grossIncome, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO)
                .newRegimeTaxSlabs(getNewRegimeTaxSlabs(newRegimeTaxableIncome))
                .taxSavings(taxSavings)
                .recommendedRegime(recommended)
                .recommendation(recommendation)
                .build();
    }

    @Override
    @Transactional
    public CapitalGainsTransaction recordCapitalGain(CapitalGainsTransaction transaction) {
        authenticationHelper.validateUserAccess(transaction.getUserId());
        CapitalGainsTransaction calculated = calculateCapitalGains(transaction);
        calculated.setCreatedDate(LocalDate.now());
        return capitalGainsRepository.save(calculated);
    }

    @Override
    @Transactional(readOnly = true)
    public CapitalGainsSummaryDTO getCapitalGainsSummary(Long userId, String financialYear) {
        authenticationHelper.validateUserAccess(userId);
        List<CapitalGainsTransaction> transactions = capitalGainsRepository.findByUserIdAndFinancialYear(userId, financialYear);
        
        BigDecimal totalSTCG = BigDecimal.ZERO;
        BigDecimal totalSTCGTax = BigDecimal.ZERO;
        BigDecimal totalLTCG = BigDecimal.ZERO;
        BigDecimal totalLTCGTax = BigDecimal.ZERO;
        BigDecimal ltcgExemption = BigDecimal.ZERO;
        
        List<CapitalGainsSummaryDTO.CapitalGainsDetail> stcgDetails = new ArrayList<>();
        List<CapitalGainsSummaryDTO.CapitalGainsDetail> ltcgDetails = new ArrayList<>();
        
        for (CapitalGainsTransaction txn : transactions) {
            CapitalGainsSummaryDTO.CapitalGainsDetail detail = CapitalGainsSummaryDTO.CapitalGainsDetail.builder()
                    .transactionId(txn.getId())
                    .assetType(txn.getAssetType())
                    .assetName(txn.getAssetName())
                    .saleDate(txn.getSaleDate().toString())
                    .purchaseDate(txn.getPurchaseDate().toString())
                    .holdingPeriodDays((int) ChronoUnit.DAYS.between(txn.getPurchaseDate(), txn.getSaleDate()))
                    .purchaseValue(txn.getTotalPurchaseValue())
                    .saleValue(txn.getTotalSaleValue())
                    .capitalGain(txn.getCapitalGain())
                    .gainType(txn.getGainType())
                    .taxAmount(txn.getTaxAmount())
                    .taxRate(txn.getTaxAmount().divide(txn.getCapitalGain(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")))
                    .build();
            
            if ("STCG".equals(txn.getGainType())) {
                totalSTCG = totalSTCG.add(txn.getCapitalGain());
                totalSTCGTax = totalSTCGTax.add(txn.getTaxAmount());
                stcgDetails.add(detail);
            } else {
                totalLTCG = totalLTCG.add(txn.getCapitalGain());
                totalLTCGTax = totalLTCGTax.add(txn.getTaxAmount());
                ltcgDetails.add(detail);
            }
        }
        
        // Calculate LTCG exemption (₹1 lakh for equity)
        if (totalLTCG.compareTo(new BigDecimal("100000")) > 0) {
            ltcgExemption = new BigDecimal("100000");
        } else {
            ltcgExemption = totalLTCG;
        }
        
        return CapitalGainsSummaryDTO.builder()
                .userId(userId)
                .financialYear(financialYear)
                .totalSTCG(totalSTCG)
                .totalSTCGTax(totalSTCGTax)
                .stcgTransactions(stcgDetails)
                .totalLTCG(totalLTCG)
                .totalLTCGTax(totalLTCGTax)
                .ltcgExemptionUsed(ltcgExemption)
                .ltcgTransactions(ltcgDetails)
                .totalCapitalGainsTax(totalSTCGTax.add(totalLTCGTax))
                .transactionCount(transactions.size())
                .build();
    }

    @Override
    public CapitalGainsTransaction calculateCapitalGains(CapitalGainsTransaction transaction) {
        // Calculate holding period
        long holdingDays = ChronoUnit.DAYS.between(transaction.getPurchaseDate(), transaction.getSaleDate());
        
        // Calculate total values
        BigDecimal totalPurchase = transaction.getPurchasePrice().multiply(transaction.getQuantity());
        BigDecimal totalSale = transaction.getSalePrice().multiply(transaction.getQuantity());
        BigDecimal capitalGain = totalSale.subtract(totalPurchase);
        
        transaction.setTotalPurchaseValue(totalPurchase);
        transaction.setTotalSaleValue(totalSale);
        transaction.setCapitalGain(capitalGain);
        
        // Determine gain type based on asset type and holding period
        String gainType;
        BigDecimal taxRate;
        
        String assetType = transaction.getAssetType();
        if ("EQUITY".equals(assetType) || 
            "MUTUAL_FUND_EQUITY".equals(assetType) ||
            "ETF".equals(assetType)) {
            // Equity: LTCG if held > 12 months
            if (holdingDays > 365) {
                gainType = "LTCG";
                taxRate = new BigDecimal("0.10"); // 10% LTCG
            } else {
                gainType = "STCG";
                taxRate = new BigDecimal("0.15"); // 15% STCG
            }
        } else if ("MUTUAL_FUND_DEBT".equals(assetType) ||
                   "BONDS".equals(assetType)) {
            // Debt: LTCG if held > 36 months (with indexation)
            if (holdingDays > 1095) {
                gainType = "LTCG";
                taxRate = new BigDecimal("0.20"); // 20% LTCG with indexation
            } else {
                gainType = "STCG";
                taxRate = new BigDecimal("0.30"); // At slab rates (assumed 30%)
            }
        } else {
            // Other assets: LTCG if held > 24 months
            if (holdingDays > 730) {
                gainType = "LTCG";
                taxRate = new BigDecimal("0.20"); // 20% LTCG
            } else {
                gainType = "STCG";
                taxRate = new BigDecimal("0.30"); // At slab rates
            }
        }
        
        transaction.setGainType(gainType);
        
        // Calculate tax
        BigDecimal taxableGain = capitalGain;
        
        // Apply LTCG exemption for equity (₹1 lakh)
        if ("LTCG".equals(gainType) && 
            ("EQUITY".equals(assetType) || 
             "MUTUAL_FUND_EQUITY".equals(assetType))) {
            taxableGain = capitalGain.subtract(new BigDecimal("100000")).max(BigDecimal.ZERO);
        }
        
        BigDecimal taxAmount = taxableGain.multiply(taxRate);
        transaction.setTaxAmount(taxAmount);
        
        return transaction;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CapitalGainsTransaction> getCapitalGainsTransactions(Long userId, String financialYear) {
        authenticationHelper.validateUserAccess(userId);
        return capitalGainsRepository.findByUserIdAndFinancialYear(userId, financialYear);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxSavingRecommendationDTO getTaxSavingRecommendations(Long userId, String financialYear) {
        authenticationHelper.validateUserAccess(userId);
        
        // Get current tax details
        List<Tax> taxes = this.repository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("financialYear"), financialYear)));
        
        BigDecimal currentIncome = BigDecimal.ZERO;
        if (!taxes.isEmpty()) {
            Tax tax = taxes.get(0);
            currentIncome = (tax.getGrossSalary() != null ? tax.getGrossSalary() : BigDecimal.ZERO)
                    .add(tax.getBusinessIncome() != null ? tax.getBusinessIncome() : BigDecimal.ZERO)
                    .add(tax.getOtherIncome() != null ? tax.getOtherIncome() : BigDecimal.ZERO);
        }
        
        // Get existing investments
        List<TaxSavingInvestment> investments = taxSavingRepository.findByUserIdAndFinancialYear(userId, financialYear);
        Map<String, BigDecimal> currentInvestments = investments.stream()
                .collect(Collectors.groupingBy(
                        TaxSavingInvestment::getSection,
                        Collectors.reducing(BigDecimal.ZERO, TaxSavingInvestment::getAmount, BigDecimal::add)
                ));
        
        // Calculate current tax liability
        BigDecimal currentTaxLiability = calculateOldRegimeTax(currentIncome);
        
        // Generate opportunities
        List<TaxSavingRecommendationDTO.TaxSavingOpportunity> opportunities = new ArrayList<>();
        
        // Section 80C
        BigDecimal current80C = currentInvestments.getOrDefault(TaxSavingSection.SECTION_80C, BigDecimal.ZERO);
        BigDecimal available80C = new BigDecimal("150000").subtract(current80C).max(BigDecimal.ZERO);
        if (available80C.compareTo(BigDecimal.ZERO) > 0) {
            opportunities.add(TaxSavingRecommendationDTO.TaxSavingOpportunity.builder()
                    .section(TaxSavingSection.SECTION_80C)
                    .currentInvestment(current80C)
                    .availableLimit(available80C)
                    .recommendedInvestment(available80C)
                    .potentialTaxSavings(available80C.multiply(new BigDecimal("0.30"))) // Assuming 30% tax bracket
                    .description("Life Insurance, PPF, EPF, ELSS, NSC, FD, etc.")
                    .priority(1)
                    .suggestedInstruments(Arrays.asList("ELSS Mutual Funds", "PPF", "EPF", "Life Insurance", "NSC"))
                    .build());
        }
        
        // Section 80D
        BigDecimal current80D = currentInvestments.getOrDefault(TaxSavingSection.SECTION_80D, BigDecimal.ZERO);
        BigDecimal available80D = new BigDecimal("25000").subtract(current80D).max(BigDecimal.ZERO);
        if (available80D.compareTo(BigDecimal.ZERO) > 0) {
            opportunities.add(TaxSavingRecommendationDTO.TaxSavingOpportunity.builder()
                    .section(TaxSavingSection.SECTION_80D)
                    .currentInvestment(current80D)
                    .availableLimit(available80D)
                    .recommendedInvestment(available80D)
                    .potentialTaxSavings(available80D.multiply(new BigDecimal("0.30")))
                    .description("Health Insurance Premium")
                    .priority(1)
                    .suggestedInstruments(Arrays.asList("Health Insurance", "Preventive Health Check-up"))
                    .build());
        }
        
        // Section 80CCD(1B) - NPS
        BigDecimal current80CCD = currentInvestments.getOrDefault(TaxSavingSection.SECTION_80CCD_1B, BigDecimal.ZERO);
        BigDecimal available80CCD = new BigDecimal("50000").subtract(current80CCD).max(BigDecimal.ZERO);
        if (available80CCD.compareTo(BigDecimal.ZERO) > 0) {
            opportunities.add(TaxSavingRecommendationDTO.TaxSavingOpportunity.builder()
                    .section(TaxSavingSection.SECTION_80CCD_1B)
                    .currentInvestment(current80CCD)
                    .availableLimit(available80CCD)
                    .recommendedInvestment(available80CCD)
                    .potentialTaxSavings(available80CCD.multiply(new BigDecimal("0.30")))
                    .description("Additional NPS Contribution")
                    .priority(2)
                    .suggestedInstruments(Arrays.asList("National Pension System (NPS)"))
                    .build());
        }
        
        BigDecimal totalPotentialSavings = opportunities.stream()
                .map(TaxSavingRecommendationDTO.TaxSavingOpportunity::getPotentialTaxSavings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalRecommendedInvestment = opportunities.stream()
                .map(TaxSavingRecommendationDTO.TaxSavingOpportunity::getRecommendedInvestment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return TaxSavingRecommendationDTO.builder()
                .userId(userId)
                .financialYear(financialYear)
                .currentIncome(currentIncome)
                .currentTaxLiability(currentTaxLiability)
                .opportunities(opportunities)
                .totalPotentialSavings(totalPotentialSavings)
                .totalRecommendedInvestment(totalRecommendedInvestment)
                .build();
    }

    @Override
    @Transactional
    public TaxSavingInvestment recordTaxSavingInvestment(TaxSavingInvestment investment) {
        authenticationHelper.validateUserAccess(investment.getUserId());
        if (investment.getCreatedDate() == null) {
            investment.setCreatedDate(LocalDate.now());
        }
        return taxSavingRepository.save(investment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaxSavingInvestment> getTaxSavingInvestments(Long userId, String financialYear) {
        authenticationHelper.validateUserAccess(userId);
        return taxSavingRepository.findByUserIdAndFinancialYear(userId, financialYear);
    }

    @Override
    @Transactional
    public TDSEntry recordTDSEntry(TDSEntry tdsEntry) {
        authenticationHelper.validateUserAccess(tdsEntry.getUserId());
        if (tdsEntry.getCreatedDate() == null) {
            tdsEntry.setCreatedDate(LocalDate.now());
        }
        tdsEntry.setUpdatedDate(LocalDate.now());
        if (tdsEntry.getStatus() == null) {
            tdsEntry.setStatus("PENDING");
        }
        return tdsRepository.save(tdsEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public TDSReconciliationDTO getTDSReconciliation(Long userId, String financialYear) {
        authenticationHelper.validateUserAccess(userId);
        List<TDSEntry> entries = tdsRepository.findByUserIdAndFinancialYear(userId, financialYear);
        
        BigDecimal totalTDS = entries.stream()
                .map(TDSEntry::getTdsAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalVerified = entries.stream()
                .filter(e -> "VERIFIED".equals(e.getStatus()) || "CLAIMED".equals(e.getStatus()))
                .map(TDSEntry::getTdsAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalClaimed = entries.stream()
                .filter(e -> "CLAIMED".equals(e.getStatus()))
                .map(TDSEntry::getTdsAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal balance = totalTDS.subtract(totalClaimed);
        
        List<TDSReconciliationDTO.TDSEntryDTO> entryDTOs = entries.stream()
                .map(e -> TDSReconciliationDTO.TDSEntryDTO.builder()
                        .id(e.getId())
                        .deductorName(e.getDeductorName())
                        .deductorTan(e.getDeductorTan())
                        .tdsAmount(e.getTdsAmount())
                        .incomeAmount(e.getIncomeAmount())
                        .tdsSection(e.getTdsSection())
                        .deductionDate(e.getDeductionDate() != null ? e.getDeductionDate().toString() : null)
                        .certificateNumber(e.getCertificateNumber())
                        .status(e.getStatus())
                        .remarks(e.getRemarks())
                        .build())
                .collect(Collectors.toList());
        
        List<String> mismatches = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            recommendations.add("You have unclaimed TDS of ₹" + balance.setScale(0, RoundingMode.HALF_UP) + ". Ensure to claim it in your ITR.");
        }
        
        long pendingCount = entries.stream().filter(e -> "PENDING".equals(e.getStatus())).count();
        if (pendingCount > 0) {
            recommendations.add(pendingCount + " TDS entries are pending verification. Verify them against Form 26AS.");
        }
        
        return TDSReconciliationDTO.builder()
                .userId(userId)
                .financialYear(financialYear)
                .totalTDSDeducted(totalTDS)
                .totalTDSVerified(totalVerified)
                .totalTDSClaimed(totalClaimed)
                .tdsBalance(balance)
                .tdsEntries(entryDTOs)
                .mismatches(mismatches)
                .recommendations(recommendations)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TDSEntry> getTDSEntries(Long userId, String financialYear) {
        authenticationHelper.validateUserAccess(userId);
        return tdsRepository.findByUserIdAndFinancialYear(userId, financialYear);
    }

    @Override
    @Transactional
    public TDSEntry updateTDSStatus(Long tdsId, String status) {
        TDSEntry entry = tdsRepository.findById(tdsId)
                .orElseThrow(() -> new RuntimeException("TDS Entry not found"));
        authenticationHelper.validateUserAccess(entry.getUserId());
        entry.setStatus(status);
        entry.setUpdatedDate(LocalDate.now());
        return tdsRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxProjectionDTO getTaxProjection(Long userId, String financialYear) {
        authenticationHelper.validateUserAccess(userId);
        
        // Get existing tax details
        List<Tax> taxes = this.repository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("financialYear"), financialYear)));
        
        Tax tax = taxes.isEmpty() ? Tax.builder().userId(userId).financialYear(financialYear).build() : taxes.get(0);
        
        // Get capital gains
        CapitalGainsSummaryDTO capitalGains = getCapitalGainsSummary(userId, financialYear);
        
        // Get TDS
        List<TDSEntry> tdsEntries = tdsRepository.findByUserIdAndFinancialYear(userId, financialYear);
        BigDecimal tdsPaid = tdsEntries.stream()
                .map(TDSEntry::getTdsAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate projections
        BigDecimal projectedSalary = tax.getGrossSalary() != null ? tax.getGrossSalary() : BigDecimal.ZERO;
        BigDecimal projectedBusiness = tax.getBusinessIncome() != null ? tax.getBusinessIncome() : BigDecimal.ZERO;
        BigDecimal projectedCapitalGains = capitalGains.getTotalSTCG().add(capitalGains.getTotalLTCG());
        BigDecimal projectedOther = tax.getOtherIncome() != null ? tax.getOtherIncome() : BigDecimal.ZERO;
        BigDecimal projectedGross = projectedSalary.add(projectedBusiness).add(projectedCapitalGains).add(projectedOther);
        
        BigDecimal standardDeduction = new BigDecimal("50000");
        BigDecimal projected80C = tax.getSection80CDeductions() != null ? tax.getSection80CDeductions() : BigDecimal.ZERO;
        BigDecimal projectedOtherDed = tax.getOtherDeductions() != null ? tax.getOtherDeductions() : BigDecimal.ZERO;
        BigDecimal totalDeductions = standardDeduction.add(projected80C).add(projectedOtherDed);
        
        BigDecimal taxableIncome = projectedGross.subtract(totalDeductions).max(BigDecimal.ZERO);
        BigDecimal taxLiability = calculateOldRegimeTax(taxableIncome);
        BigDecimal cess = taxLiability.multiply(new BigDecimal("0.04"));
        BigDecimal totalTax = taxLiability.add(cess);
        
        BigDecimal advanceTax = tax.getAdvanceTaxPaid() != null ? tax.getAdvanceTaxPaid() : BigDecimal.ZERO;
        BigDecimal selfAssessment = tax.getSelfAssessmentTax() != null ? tax.getSelfAssessmentTax() : BigDecimal.ZERO;
        BigDecimal totalPaid = tdsPaid.add(advanceTax).add(selfAssessment);
        
        BigDecimal balance = totalTax.subtract(totalPaid);
        
        // Monthly recommendations
        Map<String, String> monthlyRec = new LinkedHashMap<>();
        int currentMonth = LocalDate.now().getMonthValue();
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            monthlyRec.put("March", "Pay remaining tax of ₹" + balance.setScale(0, RoundingMode.HALF_UP) + " before March 31");
        }
        
        String planningAdvice = "Monitor your tax liability regularly. ";
        if (projected80C.compareTo(new BigDecimal("150000")) < 0) {
            planningAdvice += "Consider maximizing Section 80C deductions. ";
        }
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            planningAdvice += "Plan advance tax payments to avoid interest under Section 234B and 234C.";
        }
        
        return TaxProjectionDTO.builder()
                .userId(userId)
                .financialYear(financialYear)
                .projectedSalaryIncome(projectedSalary)
                .projectedBusinessIncome(projectedBusiness)
                .projectedCapitalGains(projectedCapitalGains)
                .projectedOtherIncome(projectedOther)
                .projectedGrossIncome(projectedGross)
                .projectedStandardDeduction(standardDeduction)
                .projected80CDeductions(projected80C)
                .projectedOtherDeductions(projectedOtherDed)
                .projectedTotalDeductions(totalDeductions)
                .projectedTaxableIncome(taxableIncome)
                .projectedTaxLiability(taxLiability)
                .projectedSurcharge(BigDecimal.ZERO)
                .projectedCess(cess)
                .projectedTotalTax(totalTax)
                .tdsPaid(tdsPaid)
                .advanceTaxPaid(advanceTax)
                .selfAssessmentTax(selfAssessment)
                .totalTaxPaid(totalPaid)
                .balanceTaxPayable(balance)
                .monthlyRecommendations(monthlyRec)
                .planningAdvice(planningAdvice)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ITRPreFillDataDTO getITRPreFillData(Long userId, String financialYear) {
        authenticationHelper.validateUserAccess(userId);
        
        // Get tax details
        List<Tax> taxes = this.repository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("userId"), userId),
                cb.equal(root.get("financialYear"), financialYear)));
        
        Tax tax = taxes.isEmpty() ? Tax.builder().userId(userId).financialYear(financialYear).build() : taxes.get(0);
        
        // Get capital gains
        List<CapitalGainsTransaction> cgTransactions = capitalGainsRepository.findByUserIdAndFinancialYear(userId, financialYear);
        BigDecimal stcg = cgTransactions.stream()
                .filter(t -> "STCG".equals(t.getGainType()))
                .map(CapitalGainsTransaction::getCapitalGain)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ltcg = cgTransactions.stream()
                .filter(t -> "LTCG".equals(t.getGainType()))
                .map(CapitalGainsTransaction::getCapitalGain)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<ITRPreFillDataDTO.CapitalGainsDetail> cgDetails = cgTransactions.stream()
                .map(t -> ITRPreFillDataDTO.CapitalGainsDetail.builder()
                        .assetDescription(t.getAssetName())
                        .dateOfPurchase(t.getPurchaseDate().toString())
                        .dateOfSale(t.getSaleDate().toString())
                        .costOfAcquisition(t.getTotalPurchaseValue())
                        .saleConsideration(t.getTotalSaleValue())
                        .capitalGain(t.getCapitalGain())
                        .gainType(t.getGainType())
                        .build())
                .collect(Collectors.toList());
        
        // Get TDS
        List<TDSEntry> tdsEntries = tdsRepository.findByUserIdAndFinancialYear(userId, financialYear);
        BigDecimal totalTDS = tdsEntries.stream()
                .map(TDSEntry::getTdsAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<ITRPreFillDataDTO.TDSDetail> tdsDetails = tdsEntries.stream()
                .map(t -> ITRPreFillDataDTO.TDSDetail.builder()
                        .deductorName(t.getDeductorName())
                        .deductorTAN(t.getDeductorTan())
                        .section(t.getTdsSection())
                        .incomeAmount(t.getIncomeAmount())
                        .tdsAmount(t.getTdsAmount())
                        .certificateNumber(t.getCertificateNumber())
                        .build())
                .collect(Collectors.toList());
        
        // Get tax saving investments
        List<TaxSavingInvestment> investments = taxSavingRepository.findByUserIdAndFinancialYear(userId, financialYear);
        Map<String, BigDecimal> deductions80C = investments.stream()
                .filter(inv -> "80C".equals(inv.getSection()))
                .collect(Collectors.groupingBy(
                        TaxSavingInvestment::getInvestmentName,
                        Collectors.reducing(BigDecimal.ZERO, TaxSavingInvestment::getAmount, BigDecimal::add)
                ));
        BigDecimal total80C = deductions80C.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, BigDecimal> otherDed = investments.stream()
                .filter(inv -> !"80C".equals(inv.getSection()))
                .collect(Collectors.groupingBy(
                        TaxSavingInvestment::getSection,
                        Collectors.reducing(BigDecimal.ZERO, TaxSavingInvestment::getAmount, BigDecimal::add)
                ));
        
        // Calculate tax
        BigDecimal grossSalary = tax.getGrossSalary() != null ? tax.getGrossSalary() : BigDecimal.ZERO;
        BigDecimal standardDed = new BigDecimal("50000");
        BigDecimal netSalary = grossSalary.subtract(standardDed).max(BigDecimal.ZERO);
        
        BigDecimal housePropertyIncome = tax.getHousePropertyIncome() != null ? tax.getHousePropertyIncome() : BigDecimal.ZERO;
        BigDecimal otherIncome = tax.getOtherIncome() != null ? tax.getOtherIncome() : BigDecimal.ZERO;
        
        BigDecimal grossTotal = netSalary.add(housePropertyIncome).add(stcg).add(ltcg).add(otherIncome);
        BigDecimal totalDeductions = total80C.add(otherDed.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal taxableIncome = grossTotal.subtract(totalDeductions).max(BigDecimal.ZERO);
        BigDecimal taxOnIncome = calculateOldRegimeTax(taxableIncome);
        BigDecimal cess = taxOnIncome.multiply(new BigDecimal("0.04"));
        BigDecimal totalTax = taxOnIncome.add(cess);
        
        BigDecimal advanceTax = tax.getAdvanceTaxPaid() != null ? tax.getAdvanceTaxPaid() : BigDecimal.ZERO;
        BigDecimal selfAssessment = tax.getSelfAssessmentTax() != null ? tax.getSelfAssessmentTax() : BigDecimal.ZERO;
        BigDecimal totalPaid = totalTDS.add(advanceTax).add(selfAssessment);
        
        BigDecimal refundOrDemand = totalTax.subtract(totalPaid);
        
        // Calculate assessment year
        String[] fyParts = financialYear.split("-");
        int startYear = Integer.parseInt(fyParts[0]);
        String assessmentYear = (startYear + 1) + "-" + (startYear + 2);
        
        return ITRPreFillDataDTO.builder()
                .userId(userId)
                .financialYear(financialYear)
                .assessmentYear(assessmentYear)
                .pan("") // To be filled by user
                .name("") // To be filled by user
                .grossSalary(grossSalary)
                .standardDeduction(standardDed)
                .netSalary(netSalary)
                .rentalIncome(housePropertyIncome)
                .housingLoanInterest(BigDecimal.ZERO)
                .netHousePropertyIncome(housePropertyIncome)
                .shortTermCapitalGains(stcg)
                .longTermCapitalGains(ltcg)
                .capitalGainsSchedule(cgDetails)
                .interestIncome(BigDecimal.ZERO)
                .dividendIncome(tax.getDividendIncome())
                .otherIncome(otherIncome)
                .deductions80C(deductions80C)
                .total80CDeductions(total80C)
                .otherDeductions(otherDed)
                .tdsDetails(tdsDetails)
                .totalTDS(totalTDS)
                .grossTotalIncome(grossTotal)
                .totalDeductions(totalDeductions)
                .taxableIncome(taxableIncome)
                .taxOnTaxableIncome(taxOnIncome)
                .surcharge(BigDecimal.ZERO)
                .healthEducationCess(cess)
                .totalTaxLiability(totalTax)
                .advanceTax(advanceTax)
                .selfAssessmentTax(selfAssessment)
                .totalTaxPaid(totalPaid)
                .refundOrDemand(refundOrDemand)
                .build();
    }

    // Helper methods for tax calculations
    
    private BigDecimal calculateOldRegimeTax(BigDecimal taxableIncome) {
        BigDecimal tax = BigDecimal.ZERO;
        
        if (taxableIncome.compareTo(new BigDecimal("250000")) <= 0) {
            return BigDecimal.ZERO;
        } else if (taxableIncome.compareTo(new BigDecimal("500000")) <= 0) {
            tax = taxableIncome.subtract(new BigDecimal("250000")).multiply(new BigDecimal("0.05"));
        } else if (taxableIncome.compareTo(new BigDecimal("1000000")) <= 0) {
            tax = new BigDecimal("12500")
                    .add(taxableIncome.subtract(new BigDecimal("500000")).multiply(new BigDecimal("0.20")));
        } else {
            tax = new BigDecimal("112500")
                    .add(taxableIncome.subtract(new BigDecimal("1000000")).multiply(new BigDecimal("0.30")));
        }
        
        return tax;
    }
    
    private BigDecimal calculateNewRegimeTax(BigDecimal taxableIncome) {
        BigDecimal tax = BigDecimal.ZERO;
        
        if (taxableIncome.compareTo(new BigDecimal("300000")) <= 0) {
            return BigDecimal.ZERO;
        } else if (taxableIncome.compareTo(new BigDecimal("600000")) <= 0) {
            tax = taxableIncome.subtract(new BigDecimal("300000")).multiply(new BigDecimal("0.05"));
        } else if (taxableIncome.compareTo(new BigDecimal("900000")) <= 0) {
            tax = new BigDecimal("15000")
                    .add(taxableIncome.subtract(new BigDecimal("600000")).multiply(new BigDecimal("0.10")));
        } else if (taxableIncome.compareTo(new BigDecimal("1200000")) <= 0) {
            tax = new BigDecimal("45000")
                    .add(taxableIncome.subtract(new BigDecimal("900000")).multiply(new BigDecimal("0.15")));
        } else if (taxableIncome.compareTo(new BigDecimal("1500000")) <= 0) {
            tax = new BigDecimal("90000")
                    .add(taxableIncome.subtract(new BigDecimal("1200000")).multiply(new BigDecimal("0.20")));
        } else {
            tax = new BigDecimal("150000")
                    .add(taxableIncome.subtract(new BigDecimal("1500000")).multiply(new BigDecimal("0.30")));
        }
        
        return tax;
    }
    
    private Map<String, BigDecimal> getOldRegimeTaxSlabs(BigDecimal taxableIncome) {
        Map<String, BigDecimal> slabs = new LinkedHashMap<>();
        
        if (taxableIncome.compareTo(new BigDecimal("250000")) > 0) {
            BigDecimal slab1 = taxableIncome.min(new BigDecimal("500000")).subtract(new BigDecimal("250000")).max(BigDecimal.ZERO);
            slabs.put("₹2.5L - ₹5L (5%)", slab1.multiply(new BigDecimal("0.05")));
        }
        if (taxableIncome.compareTo(new BigDecimal("500000")) > 0) {
            BigDecimal slab2 = taxableIncome.min(new BigDecimal("1000000")).subtract(new BigDecimal("500000")).max(BigDecimal.ZERO);
            slabs.put("₹5L - ₹10L (20%)", slab2.multiply(new BigDecimal("0.20")));
        }
        if (taxableIncome.compareTo(new BigDecimal("1000000")) > 0) {
            BigDecimal slab3 = taxableIncome.subtract(new BigDecimal("1000000"));
            slabs.put("Above ₹10L (30%)", slab3.multiply(new BigDecimal("0.30")));
        }
        
        return slabs;
    }
    
    private Map<String, BigDecimal> getNewRegimeTaxSlabs(BigDecimal taxableIncome) {
        Map<String, BigDecimal> slabs = new LinkedHashMap<>();
        
        if (taxableIncome.compareTo(new BigDecimal("300000")) > 0) {
            BigDecimal slab1 = taxableIncome.min(new BigDecimal("600000")).subtract(new BigDecimal("300000")).max(BigDecimal.ZERO);
            slabs.put("₹3L - ₹6L (5%)", slab1.multiply(new BigDecimal("0.05")));
        }
        if (taxableIncome.compareTo(new BigDecimal("600000")) > 0) {
            BigDecimal slab2 = taxableIncome.min(new BigDecimal("900000")).subtract(new BigDecimal("600000")).max(BigDecimal.ZERO);
            slabs.put("₹6L - ₹9L (10%)", slab2.multiply(new BigDecimal("0.10")));
        }
        if (taxableIncome.compareTo(new BigDecimal("900000")) > 0) {
            BigDecimal slab3 = taxableIncome.min(new BigDecimal("1200000")).subtract(new BigDecimal("900000")).max(BigDecimal.ZERO);
            slabs.put("₹9L - ₹12L (15%)", slab3.multiply(new BigDecimal("0.15")));
        }
        if (taxableIncome.compareTo(new BigDecimal("1200000")) > 0) {
            BigDecimal slab4 = taxableIncome.min(new BigDecimal("1500000")).subtract(new BigDecimal("1200000")).max(BigDecimal.ZERO);
            slabs.put("₹12L - ₹15L (20%)", slab4.multiply(new BigDecimal("0.20")));
        }
        if (taxableIncome.compareTo(new BigDecimal("1500000")) > 0) {
            BigDecimal slab5 = taxableIncome.subtract(new BigDecimal("1500000"));
            slabs.put("Above ₹15L (30%)", slab5.multiply(new BigDecimal("0.30")));
        }
        
        return slabs;
    }

    private BigDecimal calculateTotalTax(Tax tax) {
        BigDecimal stcg = tax.getCapitalGainsShortTerm() != null
                ? tax.getCapitalGainsShortTerm().multiply(new BigDecimal("0.15"))
                : BigDecimal.ZERO;
        BigDecimal ltcg = tax.getCapitalGainsLongTerm() != null
                ? tax.getCapitalGainsLongTerm().multiply(new BigDecimal("0.10"))
                : BigDecimal.ZERO;
        BigDecimal dividend = tax.getDividendIncome() != null ? tax.getDividendIncome().multiply(new BigDecimal("0.10"))
                : BigDecimal.ZERO;

        return stcg.add(ltcg).add(dividend);
    }

    private TaxDTO mapToDTO(Tax tax) {
        BigDecimal netLiability = tax.getTaxPayable() != null ? tax.getTaxPayable() : BigDecimal.ZERO;
        if (tax.getTaxPaid() != null) {
            netLiability = netLiability.subtract(tax.getTaxPaid());
        }

        return TaxDTO.builder()
                .id(tax.getId())
                .userId(tax.getUserId())
                .financialYear(tax.getFinancialYear())
                .capitalGainsShortTerm(tax.getCapitalGainsShortTerm())
                .capitalGainsLongTerm(tax.getCapitalGainsLongTerm())
                .dividendIncome(tax.getDividendIncome())
                .taxPaid(tax.getTaxPaid())
                .taxPayable(tax.getTaxPayable())
                .netTaxLiability(netLiability)
                .build();
    }
}
