package com.pisystem.modules.loans.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.pisystem.modules.loans.data.Loan;
import com.pisystem.modules.loans.data.LoanPayment;
import com.pisystem.modules.loans.dto.*;

public interface LoanService {

    Loan createLoan(Loan loan);

    List<Loan> getAllLoans();

    List<Loan> getLoansByUserId(Long userId);

    Loan getLoanById(Long id);

    void deleteLoan(Long id);

    // Calculations
    BigDecimal calculateEMI(BigDecimal principal, BigDecimal rate, Integer tenureMonths);

    Map<String, Object> simulatePrepayment(Long loanId, BigDecimal prepaymentAmount);

    // Advanced Calculations
    AmortizationScheduleResponse generateAmortizationSchedule(Long loanId);

    LoanAnalysisResponse analyzeLoan(Long loanId);

    BigDecimal calculateTotalInterest(Long loanId);

    // Payment Tracking
    LoanPayment recordPayment(RecordPaymentRequest request);

    PaymentHistoryResponse getPaymentHistory(Long loanId);

    List<LoanPayment> getMissedPayments(Long loanId);

    // Foreclosure
    ForeclosureCalculationResponse calculateForeclosure(Long loanId, BigDecimal foreclosureChargesPercentage);

    LoanPayment processForeclosure(Long loanId, BigDecimal foreclosureChargesPercentage);
}
