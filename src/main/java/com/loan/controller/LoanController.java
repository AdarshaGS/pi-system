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
}
