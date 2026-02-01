package com.loan.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.loan.data.Loan;
import com.loan.data.LoanPayment;
import com.loan.dto.*;
import com.loan.service.LoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loan", description = "Loan Management Service")
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/create")
    @Operation(summary = "Create Loan", description = "Create Loan Details of a user")
    @ApiResponse(responseCode = "200", description = "Successfully created")
    public Loan createLoan(@Valid @RequestBody Loan loan) {
        return loanService.createLoan(loan);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all loans", description = "Get all loans (Admin only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<List<Loan>> getAllLoans() {
        return new ResponseEntity<>(loanService.getAllLoans(), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get loans by user id", description = "Get loans by user id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<List<Loan>> getLoansByUserId(@PathVariable("userId") Long userId) {
        return new ResponseEntity<>(loanService.getLoansByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Loan by ID", description = "Retrieve loan details by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<Loan> getLoanById(@PathVariable("id") Long id) {
        Loan loan = loanService.getLoanById(id);
        if (loan != null) {
            return new ResponseEntity<>(loan, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Loan", description = "Delete a loan by its ID")
    @ApiResponse(responseCode = "204", description = "Successfully deleted")
    public ResponseEntity<Void> deleteLoan(@PathVariable("id") Long id) {
        loanService.deleteLoan(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/simulate-prepayment")
    @Operation(summary = "Simulate Prepayment", description = "Calculate impact of prepayment on loan tenure and interest")
    @ApiResponse(responseCode = "200", description = "Simulation result retrieved")
    public ResponseEntity<Map<String, Object>> simulatePrepayment(
            @PathVariable("id") Long id,
            @RequestParam BigDecimal amount) {
        Map<String, Object> simulation = loanService.simulatePrepayment(id, amount);
        return new ResponseEntity<>(simulation, HttpStatus.OK);
    }

    // ==================== Advanced Calculations ====================

    @GetMapping("/{id}/amortization-schedule")
    @Operation(summary = "Get Amortization Schedule", description = "Generate complete amortization schedule with interest vs principal breakdown")
    @ApiResponse(responseCode = "200", description = "Amortization schedule retrieved")
    public ResponseEntity<AmortizationScheduleResponse> getAmortizationSchedule(@PathVariable("id") Long id) {
        AmortizationScheduleResponse schedule = loanService.generateAmortizationSchedule(id);
        return new ResponseEntity<>(schedule, HttpStatus.OK);
    }

    @GetMapping("/{id}/analysis")
    @Operation(summary = "Loan Analysis", description = "Get comprehensive loan analysis including total interest, ratios, and completion percentage")
    @ApiResponse(responseCode = "200", description = "Loan analysis retrieved")
    public ResponseEntity<LoanAnalysisResponse> analyzeLoan(@PathVariable("id") Long id) {
        LoanAnalysisResponse analysis = loanService.analyzeLoan(id);
        return new ResponseEntity<>(analysis, HttpStatus.OK);
    }

    @GetMapping("/{id}/total-interest")
    @Operation(summary = "Calculate Total Interest", description = "Calculate total interest payable over the loan tenure")
    @ApiResponse(responseCode = "200", description = "Total interest calculated")
    public ResponseEntity<BigDecimal> getTotalInterest(@PathVariable("id") Long id) {
        BigDecimal totalInterest = loanService.calculateTotalInterest(id);
        return new ResponseEntity<>(totalInterest, HttpStatus.OK);
    }

    // ==================== Payment Tracking ====================

    @PostMapping("/payments")
    @Operation(summary = "Record Payment", description = "Record an EMI or prepayment for a loan")
    @ApiResponse(responseCode = "201", description = "Payment recorded successfully")
    public ResponseEntity<LoanPayment> recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        LoanPayment payment = loanService.recordPayment(request);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/payments")
    @Operation(summary = "Get Payment History", description = "Get complete payment history for a loan")
    @ApiResponse(responseCode = "200", description = "Payment history retrieved")
    public ResponseEntity<PaymentHistoryResponse> getPaymentHistory(@PathVariable("id") Long id) {
        PaymentHistoryResponse history = loanService.getPaymentHistory(id);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    @GetMapping("/{id}/missed-payments")
    @Operation(summary = "Get Missed Payments", description = "Get all missed payments for a loan")
    @ApiResponse(responseCode = "200", description = "Missed payments retrieved")
    public ResponseEntity<List<LoanPayment>> getMissedPayments(@PathVariable("id") Long id) {
        List<LoanPayment> missedPayments = loanService.getMissedPayments(id);
        return new ResponseEntity<>(missedPayments, HttpStatus.OK);
    }

    // ==================== Foreclosure ====================

    @GetMapping("/{id}/foreclosure-calculation")
    @Operation(summary = "Calculate Foreclosure Amount", description = "Calculate the total amount required to foreclose the loan")
    @ApiResponse(responseCode = "200", description = "Foreclosure calculation completed")
    public ResponseEntity<ForeclosureCalculationResponse> calculateForeclosure(
            @PathVariable("id") Long id,
            @RequestParam(required = false, defaultValue = "0") BigDecimal foreclosureChargesPercentage) {
        ForeclosureCalculationResponse calculation = loanService.calculateForeclosure(id, foreclosureChargesPercentage);
        return new ResponseEntity<>(calculation, HttpStatus.OK);
    }

    @PostMapping("/{id}/foreclose")
    @Operation(summary = "Process Foreclosure", description = "Process loan foreclosure and record the foreclosure payment")
    @ApiResponse(responseCode = "200", description = "Loan foreclosed successfully")
    public ResponseEntity<LoanPayment> processForeclosure(
            @PathVariable("id") Long id,
            @RequestParam(required = false, defaultValue = "0") BigDecimal foreclosureChargesPercentage) {
        LoanPayment foreclosurePayment = loanService.processForeclosure(id, foreclosureChargesPercentage);
        return new ResponseEntity<>(foreclosurePayment, HttpStatus.OK);
    }
}
