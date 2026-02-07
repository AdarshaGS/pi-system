package com.mutualfund.controller;

import com.common.security.AuthenticationHelper;
import com.mutualfund.model.MutualFundTransaction;
import com.mutualfund.service.MutualFundTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/mutual-funds/transactions")
public class MutualFundTransactionController {

    @Autowired
    private MutualFundTransactionService transactionService;

    @Autowired
    private AuthenticationHelper authenticationHelper;

    @PostMapping
    public ResponseEntity<MutualFundTransaction> addTransaction(@RequestBody MutualFundTransaction transaction) {
        Long userId = authenticationHelper.getCurrentUserId();
        MutualFundTransaction created = transactionService.addTransaction(userId, transaction);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MutualFundTransaction>> getAllTransactions() {
        Long userId = authenticationHelper.getCurrentUserId();
        List<MutualFundTransaction> transactions = transactionService.getAllTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/fund/{fundId}")
    public ResponseEntity<List<MutualFundTransaction>> getTransactionsByFund(@PathVariable Long fundId) {
        Long userId = authenticationHelper.getCurrentUserId();
        List<MutualFundTransaction> transactions = transactionService.getTransactionsByFund(userId, fundId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<MutualFundTransaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = authenticationHelper.getCurrentUserId();
        List<MutualFundTransaction> transactions = transactionService.getTransactionsByDateRange(userId, startDate,
                endDate);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MutualFundTransaction> updateTransaction(
            @PathVariable Long id,
            @RequestBody MutualFundTransaction transaction) {
        Long userId = authenticationHelper.getCurrentUserId();
        MutualFundTransaction updated = transactionService.updateTransaction(userId, id, transaction);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        Long userId = authenticationHelper.getCurrentUserId();
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.noContent().build();
    }
}
