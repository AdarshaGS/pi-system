package com.tax.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tax.data.Tax;
import com.tax.data.TaxDTO;
import com.tax.repo.TaxRepository;

@Service
public class TaxServiceImpl implements TaxService {

    private final TaxRepository repository;

    public TaxServiceImpl(final TaxRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public TaxDTO createTaxDetails(Tax tax) {
        
        // Calculate net tax liability
        BigDecimal totalTax = calculateTotalTax(tax);
        tax.setTaxPayable(totalTax);
        if (tax.getCreatedDate() == null) {
            tax.setCreatedDate(LocalDate.now());
        }
        Tax saved = this.repository.save(tax);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxDTO getTaxDetailsByUserId(Long userId, String financialYear) {
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
        List<Tax> taxes = this.repository.findAll((root, query, cb) -> cb.equal(root.get("userId"), userId));

        return taxes.stream()
                .map(tax -> {
                    BigDecimal payable = tax.getTaxPayable() != null ? tax.getTaxPayable() : BigDecimal.ZERO;
                    BigDecimal paid = tax.getTaxPaid() != null ? tax.getTaxPaid() : BigDecimal.ZERO;
                    return payable.subtract(paid);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
