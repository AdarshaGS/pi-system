package com.investments.stocks.controller;

import com.investments.stocks.data.RecurringTransaction;
import com.investments.stocks.dto.RecurringTransactionDTO;
import com.investments.stocks.dto.RecurringTransactionHistoryDTO;
import com.investments.stocks.service.InvestmentRecurringTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
public class InvestmentRecurringTransactionController {
    
    private final InvestmentRecurringTransactionService recurringTransactionService;
    
    public InvestmentRecurringTransactionController(InvestmentRecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }
    
    @PostMapping
    public ResponseEntity<RecurringTransactionDTO> createRecurringTransaction(
            @RequestBody RecurringTransactionDTO dto) {
        RecurringTransactionDTO created = recurringTransactionService.createRecurringTransaction(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionDTO> updateRecurringTransaction(
            @PathVariable Long id,
            @RequestBody RecurringTransactionDTO dto) {
        RecurringTransactionDTO updated = recurringTransactionService.updateRecurringTransaction(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RecurringTransactionDTO> getRecurringTransaction(@PathVariable Long id) {
        RecurringTransactionDTO transaction = recurringTransactionService.getRecurringTransaction(id);
        return ResponseEntity.ok(transaction);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecurringTransactionDTO>> getUserRecurringTransactions(@PathVariable Long userId) {
        List<RecurringTransactionDTO> transactions = recurringTransactionService.getUserRecurringTransactions(userId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<RecurringTransactionDTO>> getActiveRecurringTransactions(@PathVariable Long userId) {
        List<RecurringTransactionDTO> transactions = recurringTransactionService.getActiveRecurringTransactions(userId);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<RecurringTransactionDTO>> getRecurringTransactionsByType(
            @PathVariable Long userId,
            @PathVariable RecurringTransaction.TransactionType type) {
        List<RecurringTransactionDTO> transactions = recurringTransactionService.getRecurringTransactionsByType(userId, type);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<RecurringTransactionDTO>> getUpcomingTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        List<RecurringTransactionDTO> transactions = recurringTransactionService.getUpcomingTransactions(userId, days);
        return ResponseEntity.ok(transactions);
    }
    
    @PostMapping("/{id}/pause")
    public ResponseEntity<Void> pauseRecurringTransaction(@PathVariable Long id) {
        recurringTransactionService.pauseRecurringTransaction(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/resume")
    public ResponseEntity<Void> resumeRecurringTransaction(@PathVariable Long id) {
        recurringTransactionService.resumeRecurringTransaction(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRecurringTransaction(@PathVariable Long id) {
        recurringTransactionService.cancelRecurringTransaction(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringTransaction(@PathVariable Long id) {
        recurringTransactionService.deleteRecurringTransaction(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/execute")
    public ResponseEntity<RecurringTransactionHistoryDTO> executeRecurringTransaction(@PathVariable Long id) {
        RecurringTransactionHistoryDTO history = recurringTransactionService.executeRecurringTransaction(id);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/{id}/history")
    public ResponseEntity<List<RecurringTransactionHistoryDTO>> getTransactionHistory(@PathVariable Long id) {
        List<RecurringTransactionHistoryDTO> history = recurringTransactionService.getTransactionHistory(id);
        return ResponseEntity.ok(history);
    }
    
    @PostMapping("/process-due")
    public ResponseEntity<Void> processDueTransactions() {
        recurringTransactionService.processDueTransactions();
        return ResponseEntity.ok().build();
    }
}
