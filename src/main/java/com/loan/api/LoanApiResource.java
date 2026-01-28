package com.loan.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.auth.security.UserSecurity;
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
public class LoanApiResource {

    private final LoanService loanService;
    private final UserSecurity userSecurity;

    @PostMapping("/create")
    @Operation(summary = "Create Loan", description = "Create Loan Details of a user")
    @ApiResponse(responseCode = "200", description = "Successfully created")
    @PreAuthorize("@userSecurity.hasUserId(#loan.userId)")
    public Loan createLoan(@RequestBody Loan loan) {
        return loanService.createLoan(loan);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all loans", description = "Get all loans (Admin only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getAllLoans() {
        return new ResponseEntity<>(loanService.getAllLoans(), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get loans by user id", description = "Get loans by user id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @PreAuthorize("@userSecurity.hasUserId(#userId)")
    public ResponseEntity<List<Loan>> getLoansByUserId(@PathVariable Long userId) {
        return new ResponseEntity<>(loanService.getLoansByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Loan by ID", description = "Retrieve loan details by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @PreAuthorize("isAuthenticated()") // Service layer should check ownership if we don't have userId in Path
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        Loan loan = loanService.getLoanById(id);
        if (loan != null && userSecurity.hasUserId(loan.getUserId())) {
            return new ResponseEntity<>(loan, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Loan", description = "Delete a loan by its ID")
    @ApiResponse(responseCode = "204", description = "Successfully deleted")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        Loan loan = loanService.getLoanById(id);
        if (loan != null && userSecurity.hasUserId(loan.getUserId())) {
            loanService.deleteLoan(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @PostMapping("/{id}/simulate-prepayment")
    @Operation(summary = "Simulate Prepayment", description = "Calculate impact of prepayment on loan tenure and interest")
    @ApiResponse(responseCode = "200", description = "Simulation result retrieved")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> simulatePrepayment(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        Loan loan = loanService.getLoanById(id);
        if (loan != null && userSecurity.hasUserId(loan.getUserId())) {
            Map<String, Object> simulation = loanService.simulatePrepayment(id, amount);
            return new ResponseEntity<>(simulation, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
