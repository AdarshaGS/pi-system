package com.loan.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.security.AuthenticationHelper;
import com.loan.data.Loan;
import com.loan.data.LoanPayment;
import com.loan.data.PaymentStatus;
import com.loan.data.PaymentType;
import com.loan.dto.*;
import com.loan.repo.LoanPaymentRepository;
import com.loan.repo.LoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final AuthenticationHelper authenticationHelper;
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal TWELVE_HUNDRED = BigDecimal.valueOf(1200);

    @Override
    @Transactional
    public Loan createLoan(Loan loan) {
        authenticationHelper.validateUserAccess(loan.getUserId());
        if (loan.getEmiAmount() == null && loan.getPrincipalAmount() != null
                && loan.getInterestRate() != null && loan.getTenureMonths() != null) {
            loan.setEmiAmount(calculateEMI(loan.getPrincipalAmount(), loan.getInterestRate(), loan.getTenureMonths()));
        }
        if (loan.getOutstandingAmount() == null) {
            loan.setOutstandingAmount(loan.getPrincipalAmount());
        }
        calculateEndDate(loan);
        return loanRepository.save(loan);
    }

    private void calculateEndDate(Loan loan) {
        if (loan.getStartDate() != null && loan.getTenureMonths() != null) {
            loan.setEndDate(loan.getStartDate().plusMonths(loan.getTenureMonths()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> getAllLoans() {
        authenticationHelper.validateAdminAccess();
        return loanRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> getLoansByUserId(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return loanRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Loan getLoanById(Long id) {
        Loan loan = loanRepository.findById(id).orElse(null);
        if (loan != null) {
            authenticationHelper.validateUserAccess(loan.getUserId());
        }
        return loan;
    }

    @Override
    @Transactional
    public void deleteLoan(Long id) {
        Loan loan = loanRepository.findById(id).orElse(null);
        if (loan != null) {
            authenticationHelper.validateUserAccess(loan.getUserId());
            loanRepository.deleteById(id);
        }
    }

    @Override
    public BigDecimal calculateEMI(BigDecimal principal, BigDecimal rate, Integer tenureMonths) {
        if (principal == null || rate == null || tenureMonths == null || tenureMonths == 0) {
            return BigDecimal.ZERO;
        }

        // Handle zero interest rate (0% loan)
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        // r = rate / 12 / 100
        BigDecimal monthlyRate = rate.divide(TWELVE_HUNDRED, MC);

        // Calculate (1+r)^n using manual calculation to avoid precision loss
        BigDecimal onePlusR = monthlyRate.add(BigDecimal.ONE);
        BigDecimal onePlusRToN = BigDecimal.ONE;
        for (int i = 0; i < tenureMonths; i++) {
            onePlusRToN = onePlusRToN.multiply(onePlusR, MC);
        }

        // Numerator: P * r * (1+r)^n
        BigDecimal numerator = principal.multiply(monthlyRate, MC).multiply(onePlusRToN, MC);

        // Denominator: (1+r)^n - 1
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);

        if (denominator.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> simulatePrepayment(Long loanId, BigDecimal prepaymentAmount) {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new RuntimeException("Loan not found");
        }

        BigDecimal currentOutstanding = loan.getOutstandingAmount();
        BigDecimal newPrincipal = currentOutstanding.subtract(prepaymentAmount);

        if (newPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Loan fully paid off!");
            result.put("newTenureMonths", 0);
            result.put("savedInterest", currentOutstanding.subtract(prepaymentAmount).abs());
            return result;
        }

        // Calculate new tenure keeping EMI same
        // Formula: n = log(EMI / (EMI - P*r)) / log(1+r)

        BigDecimal emi = loan.getEmiAmount();
        BigDecimal interestRate = loan.getInterestRate();
        
        // Handle zero interest rate
        if (interestRate.compareTo(BigDecimal.ZERO) == 0) {
            int newTenureMonths = newPrincipal.divide(emi, 0, RoundingMode.UP).intValue();
            Map<String, Object> result = new HashMap<>();
            result.put("originalTenureMonths", loan.getTenureMonths());
            result.put("remainingTenureMonths", currentOutstanding.divide(emi, 0, RoundingMode.UP).intValue());
            result.put("newTenureMonths", newTenureMonths);
            result.put("savedInterest", BigDecimal.ZERO);
            return result;
        }
        
        BigDecimal monthlyRate = interestRate.divide(TWELVE_HUNDRED, MC);
        BigDecimal pTimesR = newPrincipal.multiply(monthlyRate); // P*r

        // If P*r >= EMI, unlimited tenure (debt trap). Should check.
        if (pTimesR.compareTo(emi) >= 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "Prepayment insufficient to reduce tenure with current EMI");
            return result;
        }

        BigDecimal emiMinusPR = emi.subtract(pTimesR); // EMI - P*r
        BigDecimal ratio = emi.divide(emiMinusPR, MC); // EMI / (EMI - P*r)

        double logRatio = Math.log(ratio.doubleValue());
        double logOnePlusR = Math.log(monthlyRate.add(BigDecimal.ONE).doubleValue());

        double newTenure = logRatio / logOnePlusR;
        int newTenureMonths = (int) Math.ceil(newTenure);

        // Calculate Saved Interest
        // Original Interest Remaining (approx): (EMI * RemainingTenure) -
        // CurrentOutstanding
        // We need RemainingTenure of the ORIGINAL loan at this point.
        // n_orig = log(EMI / (EMI - Outstanding*r)) / log(1+r)

        BigDecimal pTimesROrig = currentOutstanding.multiply(monthlyRate);
        BigDecimal emiMinusPROrig = emi.subtract(pTimesROrig);
        BigDecimal ratioOrig = emi.divide(emiMinusPROrig, MC);
        double logRatioOrig = Math.log(ratioOrig.doubleValue());
        double currentRemainingTenure = logRatioOrig / logOnePlusR;
        int currentRemainingMonths = (int) Math.ceil(currentRemainingTenure);

        BigDecimal originalRemainingInterest = emi.multiply(BigDecimal.valueOf(currentRemainingMonths))
                .subtract(currentOutstanding);
        BigDecimal newRemainingInterest = emi.multiply(BigDecimal.valueOf(newTenureMonths)).subtract(newPrincipal);

        BigDecimal savedInterest = originalRemainingInterest.subtract(newRemainingInterest);
        if (savedInterest.compareTo(BigDecimal.ZERO) < 0)
            savedInterest = BigDecimal.ZERO;

        Map<String, Object> result = new HashMap<>();
        result.put("originalTenureMonths", loan.getTenureMonths()); // Total original
        result.put("remainingTenureMonths", currentRemainingMonths);
        result.put("newTenureMonths", newTenureMonths);
        result.put("savedInterest", savedInterest.setScale(2, RoundingMode.HALF_UP));

        return result;
    }

    // ==================== Advanced Calculations ====================

    @Override
    @Transactional(readOnly = true)
    public AmortizationScheduleResponse generateAmortizationSchedule(Long loanId) {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new RuntimeException("Loan not found");
        }

        BigDecimal principal = loan.getPrincipalAmount();
        BigDecimal rate = loan.getInterestRate();
        Integer tenureMonths = loan.getTenureMonths();
        BigDecimal emi = loan.getEmiAmount();
        LocalDate startDate = loan.getStartDate();

        if (principal == null || rate == null || tenureMonths == null || emi == null) {
            throw new RuntimeException("Incomplete loan data for amortization schedule");
        }

        BigDecimal monthlyRate = rate.divide(TWELVE_HUNDRED, MC);
        BigDecimal balance = principal;
        BigDecimal totalInterest = BigDecimal.ZERO;

        List<AmortizationScheduleResponse.AmortizationEntry> schedule = new ArrayList<>();

        for (int i = 1; i <= tenureMonths; i++) {
            BigDecimal interestForMonth = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalForMonth = emi.subtract(interestForMonth).setScale(2, RoundingMode.HALF_UP);

            // Adjust for last payment
            if (i == tenureMonths || principalForMonth.compareTo(balance) > 0) {
                principalForMonth = balance;
                interestForMonth = emi.subtract(principalForMonth);
            }

            balance = balance.subtract(principalForMonth).setScale(2, RoundingMode.HALF_UP);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                balance = BigDecimal.ZERO;
            }

            totalInterest = totalInterest.add(interestForMonth);

            LocalDate paymentDate = startDate != null ? startDate.plusMonths(i) : null;

            AmortizationScheduleResponse.AmortizationEntry entry = AmortizationScheduleResponse.AmortizationEntry.builder()
                    .paymentNumber(i)
                    .paymentDate(paymentDate)
                    .emiAmount(emi)
                    .principalComponent(principalForMonth)
                    .interestComponent(interestForMonth)
                    .outstandingBalance(balance)
                    .build();

            schedule.add(entry);
        }

        return AmortizationScheduleResponse.builder()
                .loanId(loanId)
                .totalPrincipal(principal)
                .totalInterest(totalInterest.setScale(2, RoundingMode.HALF_UP))
                .totalPayable(principal.add(totalInterest).setScale(2, RoundingMode.HALF_UP))
                .tenureMonths(tenureMonths)
                .schedule(schedule)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanAnalysisResponse analyzeLoan(Long loanId) {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new RuntimeException("Loan not found");
        }

        BigDecimal totalInterest = calculateTotalInterest(loanId);
        BigDecimal totalPayable = loan.getPrincipalAmount().add(totalInterest);
        BigDecimal interestToPrincipalRatio = totalInterest
                .divide(loan.getPrincipalAmount(), 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED);

        // Calculate remaining tenure based on current outstanding
        BigDecimal monthlyRate = loan.getInterestRate().divide(TWELVE_HUNDRED, MC);
        BigDecimal emi = loan.getEmiAmount();
        BigDecimal outstanding = loan.getOutstandingAmount();

        int remainingMonths = 0;
        BigDecimal remainingInterest = BigDecimal.ZERO;

        if (outstanding.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pTimesR = outstanding.multiply(monthlyRate);
            if (pTimesR.compareTo(emi) < 0) {
                BigDecimal emiMinusPR = emi.subtract(pTimesR);
                BigDecimal ratio = emi.divide(emiMinusPR, MC);
                double logRatio = Math.log(ratio.doubleValue());
                double logOnePlusR = Math.log(monthlyRate.add(BigDecimal.ONE).doubleValue());
                remainingMonths = (int) Math.ceil(logRatio / logOnePlusR);

                remainingInterest = emi.multiply(BigDecimal.valueOf(remainingMonths))
                        .subtract(outstanding)
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }

        List<LoanPayment> payments = loanPaymentRepository.findByLoanIdAndPaymentStatus(loanId, PaymentStatus.PAID);
        int paymentsCompleted = payments.size();
        int totalPayments = loan.getTenureMonths();
        BigDecimal completionPercentage = BigDecimal.valueOf(paymentsCompleted)
                .divide(BigDecimal.valueOf(totalPayments), 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED);

        return LoanAnalysisResponse.builder()
                .loanId(loanId)
                .totalInterestPayable(totalInterest)
                .totalAmountPayable(totalPayable)
                .interestToPrincipalRatio(interestToPrincipalRatio)
                .effectiveInterestRate(loan.getInterestRate())
                .remainingTenureMonths(remainingMonths)
                .remainingInterest(remainingInterest)
                .paymentsCompleted(paymentsCompleted)
                .totalPayments(totalPayments)
                .completionPercentage(completionPercentage)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalInterest(Long loanId) {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new RuntimeException("Loan not found");
        }

        BigDecimal totalPayable = loan.getEmiAmount().multiply(BigDecimal.valueOf(loan.getTenureMonths()));
        return totalPayable.subtract(loan.getPrincipalAmount()).setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== Payment Tracking ====================

    @Override
    @Transactional
    public LoanPayment recordPayment(RecordPaymentRequest request) {
        Loan loan = getLoanById(request.getLoanId());
        if (loan == null) {
            throw new RuntimeException("Loan not found");
        }

        authenticationHelper.validateUserAccess(loan.getUserId());

        BigDecimal paymentAmount = request.getPaymentAmount();
        BigDecimal outstandingBalance = loan.getOutstandingAmount();
        
        BigDecimal interestPaid;
        BigDecimal principalPaid;

        // For prepayment or foreclosure, entire amount goes to principal (no interest)
        if (request.getPaymentType() == PaymentType.PREPAYMENT || 
            request.getPaymentType() == PaymentType.FORECLOSURE) {
            principalPaid = paymentAmount;
            interestPaid = BigDecimal.ZERO;
        } else {
            // For regular EMI payment, calculate interest and principal components
            BigDecimal monthlyRate = loan.getInterestRate().divide(TWELVE_HUNDRED, MC);
            interestPaid = outstandingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            principalPaid = paymentAmount.subtract(interestPaid).setScale(2, RoundingMode.HALF_UP);
            
            // Ensure principal paid doesn't exceed outstanding
            if (principalPaid.compareTo(outstandingBalance) > 0) {
                principalPaid = outstandingBalance;
                interestPaid = paymentAmount.subtract(principalPaid).setScale(2, RoundingMode.HALF_UP);
            }
        }

        // Update outstanding balance
        BigDecimal newOutstanding = outstandingBalance.subtract(principalPaid).setScale(2, RoundingMode.HALF_UP);
        if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            newOutstanding = BigDecimal.ZERO;
        }

        loan.setOutstandingAmount(newOutstanding);
        loanRepository.save(loan);

        // Create payment record
        LoanPayment payment = new LoanPayment();
        payment.setLoanId(request.getLoanId());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentAmount(paymentAmount);
        payment.setPrincipalPaid(principalPaid);
        payment.setInterestPaid(interestPaid);
        payment.setOutstandingBalance(newOutstanding);
        payment.setPaymentType(request.getPaymentType());
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionReference(request.getTransactionReference());
        payment.setNotes(request.getNotes());
        payment.setCreatedAt(LocalDate.now());

        return loanPaymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentHistoryResponse getPaymentHistory(Long loanId) {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new RuntimeException("Loan not found");
        }

        authenticationHelper.validateUserAccess(loan.getUserId());

        List<LoanPayment> payments = loanPaymentRepository.findByLoanIdOrderByPaymentDateDesc(loanId);
        
        long missedPaymentsCount = loanPaymentRepository.countMissedPaymentsByLoanId(loanId);

        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        BigDecimal totalInterestPaid = BigDecimal.ZERO;

        List<PaymentHistoryResponse.PaymentSummary> paymentSummaries = new ArrayList<>();

        for (LoanPayment payment : payments) {
            if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                totalPaid = totalPaid.add(payment.getPaymentAmount());
                totalPrincipalPaid = totalPrincipalPaid.add(payment.getPrincipalPaid());
                totalInterestPaid = totalInterestPaid.add(payment.getInterestPaid());
            }

            PaymentHistoryResponse.PaymentSummary summary = PaymentHistoryResponse.PaymentSummary.builder()
                    .paymentId(payment.getId())
                    .paymentDate(payment.getPaymentDate().toString())
                    .amount(payment.getPaymentAmount())
                    .principalPaid(payment.getPrincipalPaid())
                    .interestPaid(payment.getInterestPaid())
                    .paymentType(payment.getPaymentType().toString())
                    .paymentStatus(payment.getPaymentStatus().toString())
                    .paymentMethod(payment.getPaymentMethod())
                    .build();

            paymentSummaries.add(summary);
        }

        return PaymentHistoryResponse.builder()
                .loanId(loanId)
                .totalPayments(payments.size())
                .missedPayments((int) missedPaymentsCount)
                .totalPaid(totalPaid.setScale(2, RoundingMode.HALF_UP))
                .totalPrincipalPaid(totalPrincipalPaid.setScale(2, RoundingMode.HALF_UP))
                .totalInterestPaid(totalInterestPaid.setScale(2, RoundingMode.HALF_UP))
                .outstandingBalance(loan.getOutstandingAmount())
                .payments(paymentSummaries)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanPayment> getMissedPayments(Long loanId) {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new RuntimeException("Loan not found");
        }

        authenticationHelper.validateUserAccess(loan.getUserId());
        return loanPaymentRepository.findMissedPaymentsByLoanId(loanId);
    }

    // ==================== Foreclosure ====================

    @Override
    @Transactional(readOnly = true)
    public ForeclosureCalculationResponse calculateForeclosure(Long loanId, BigDecimal foreclosureChargesPercentage) {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new RuntimeException("Loan not found");
        }

        authenticationHelper.validateUserAccess(loan.getUserId());

        BigDecimal outstandingPrincipal = loan.getOutstandingAmount();
        
        // Calculate outstanding interest for the current month
        BigDecimal monthlyRate = loan.getInterestRate().divide(TWELVE_HUNDRED, MC);
        BigDecimal outstandingInterest = outstandingPrincipal.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);

        // Calculate foreclosure charges
        BigDecimal foreclosureCharges = BigDecimal.ZERO;
        if (foreclosureChargesPercentage != null && foreclosureChargesPercentage.compareTo(BigDecimal.ZERO) > 0) {
            foreclosureCharges = outstandingPrincipal
                    .multiply(foreclosureChargesPercentage)
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalForeclosureAmount = outstandingPrincipal
                .add(outstandingInterest)
                .add(foreclosureCharges)
                .setScale(2, RoundingMode.HALF_UP);

        return ForeclosureCalculationResponse.builder()
                .loanId(loanId)
                .outstandingPrincipal(outstandingPrincipal)
                .outstandingInterest(outstandingInterest)
                .foreclosureCharges(foreclosureCharges)
                .foreclosureChargesPercentage(foreclosureChargesPercentage != null ? foreclosureChargesPercentage : BigDecimal.ZERO)
                .totalForeclosureAmount(totalForeclosureAmount)
                .message("Loan can be foreclosed by paying the total foreclosure amount")
                .build();
    }

    @Override
    @Transactional
    public LoanPayment processForeclosure(Long loanId, BigDecimal foreclosureChargesPercentage) {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new RuntimeException("Loan not found");
        }

        authenticationHelper.validateUserAccess(loan.getUserId());

        ForeclosureCalculationResponse calculation = calculateForeclosure(loanId, foreclosureChargesPercentage);

        // Create foreclosure payment record
        LoanPayment foreclosurePayment = new LoanPayment();
        foreclosurePayment.setLoanId(loanId);
        foreclosurePayment.setPaymentDate(LocalDate.now());
        foreclosurePayment.setPaymentAmount(calculation.getTotalForeclosureAmount());
        foreclosurePayment.setPrincipalPaid(calculation.getOutstandingPrincipal());
        foreclosurePayment.setInterestPaid(calculation.getOutstandingInterest().add(calculation.getForeclosureCharges()));
        foreclosurePayment.setOutstandingBalance(BigDecimal.ZERO);
        foreclosurePayment.setPaymentType(PaymentType.FORECLOSURE);
        foreclosurePayment.setPaymentStatus(PaymentStatus.PAID);
        foreclosurePayment.setNotes("Loan foreclosure with " + foreclosureChargesPercentage + "% charges");
        foreclosurePayment.setCreatedAt(LocalDate.now());

        // Update loan outstanding to zero
        loan.setOutstandingAmount(BigDecimal.ZERO);
        loanRepository.save(loan);

        return loanPaymentRepository.save(foreclosurePayment);
    }
}
