package com.pisystem.modules.etf.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pisystem.modules.etf.service.ETFTransactionService;
import com.pisystem.modules.etf.model.ETFTransaction;
import com.pisystem.shared.security.AuthenticationHelper;

@RestController
@RequestMapping("/api/v1/etfs/transactions")
public class ETFTransactionController {

    @Autowired
    private ETFTransactionService transactionService;

    @Autowired
    private AuthenticationHelper authenticationHelper;

    @PostMapping
    public ResponseEntity<ETFTransaction> addTransaction(@RequestBody ETFTransaction transaction) {
        Long userId = authenticationHelper.getCurrentUserId();
        ETFTransaction created = this.transactionService.addTransaction(userId, transaction);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ETFTransaction>> getAllTransactions() {
        Long userId = authenticationHelper.getCurrentUserId();
        List<ETFTransaction> transactions = transactionService.getAllTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/etf/{etfId}")
    public ResponseEntity<List<ETFTransaction>> getTransactionsByETF(@PathVariable Long etfId) {
        Long userId = authenticationHelper.getCurrentUserId();
        List<ETFTransaction> transactions = transactionService.getTransactionsByETF(userId, etfId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ETFTransaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = authenticationHelper.getCurrentUserId();
        List<ETFTransaction> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ETFTransaction> updateTransaction(
            @PathVariable Long id,
            @RequestBody ETFTransaction transaction) {
        Long userId = authenticationHelper.getCurrentUserId();
        ETFTransaction updated = transactionService.updateTransaction(userId, id, transaction);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        Long userId = authenticationHelper.getCurrentUserId();
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.noContent().build();
    }
}
