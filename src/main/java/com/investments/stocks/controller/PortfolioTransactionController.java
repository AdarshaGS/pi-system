package com.investments.stocks.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.security.AuthenticationHelper;
import com.investments.stocks.data.PortfolioTransaction;
import com.investments.stocks.dto.PortfolioTransactionRequest;
import com.investments.stocks.dto.TransactionStats;
import com.investments.stocks.service.PortfolioTransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Portfolio Transaction Management
 * Handles all portfolio transaction operations (Buy, Sell, Dividend, etc.)
 */
@RestController
@RequestMapping("/api/v1/portfolio/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Portfolio Transactions", description = "APIs for managing portfolio transactions")
public class PortfolioTransactionController {

    private final PortfolioTransactionService transactionService;
    private final AuthenticationHelper authHelper;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER_READ_ONLY', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Record a new transaction",
        description = "Record a BUY, SELL, DIVIDEND, or other transaction type. SELL transactions automatically calculate realized gains using FIFO method.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Transaction recorded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<PortfolioTransaction> recordTransaction(
            @Valid @RequestBody PortfolioTransactionRequest request,
            @RequestHeader("Authorization") String authToken) {
        
        Long userId = authHelper.getCurrentUserId();
        request.setUserId(userId);
        
        log.info("Recording transaction for user {}: {} {} shares of {}", 
                userId, request.getTransactionType(), request.getQuantity(), request.getSymbol());
        
        PortfolioTransaction transaction = transactionService.recordTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER_READ_ONLY', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Get all transactions for a user",
        description = "Retrieve all portfolio transactions for the authenticated user, ordered by date (newest first)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<List<PortfolioTransaction>> getAllTransactions(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authToken) {
        
        Long authenticatedUserId = authHelper.getCurrentUserId();
        authHelper.validateUserAccess(authenticatedUserId);
        
        log.info("Fetching all transactions for user {}", userId);
        List<PortfolioTransaction> transactions = transactionService.getAllTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{userId}/symbol/{symbol}")
    @PreAuthorize("hasAnyRole('USER_READ_ONLY', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Get transactions by symbol",
        description = "Retrieve all transactions for a specific stock symbol",
        responses = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<List<PortfolioTransaction>> getTransactionsBySymbol(
            @PathVariable Long userId,
            @PathVariable @Parameter(description = "Stock symbol (e.g., RELIANCE)", example = "RELIANCE") String symbol,
            @RequestHeader("Authorization") String authToken) {
        
        Long authenticatedUserId = authHelper.getCurrentUserId();
        authHelper.validateUserAccess(authenticatedUserId);
        
        log.info("Fetching transactions for user {} and symbol {}", userId, symbol);
        List<PortfolioTransaction> transactions = transactionService.getTransactionsBySymbol(userId, symbol);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transaction/{id}")
    @PreAuthorize("hasAnyRole('USER_READ_ONLY', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Get transaction by ID",
        description = "Retrieve a specific transaction by its ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<PortfolioTransaction> getTransactionById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authToken) {
        
        authHelper.getCurrentUserId(); // Validate authentication
        
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER_READ_ONLY', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Update a transaction",
        description = "Update an existing transaction. User can only update their own transactions.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<PortfolioTransaction> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody PortfolioTransactionRequest request,
            @RequestHeader("Authorization") String authToken) {
        
        Long userId = authHelper.getCurrentUserId();
        request.setUserId(userId);
        
        log.info("Updating transaction {} for user {}", id, userId);
        PortfolioTransaction updated = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER_READ_ONLY', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Delete a transaction",
        description = "Delete a portfolio transaction. This cannot be undone.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Transaction deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<Map<String, String>> deleteTransaction(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authToken) {
        
        authHelper.getCurrentUserId(); // Validate authentication
        
        log.info("Deleting transaction {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully", "id", id.toString()));
    }

    @GetMapping("/{userId}/stats")
    @PreAuthorize("hasAnyRole('USER_READ_ONLY', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Get transaction statistics",
        description = "Get comprehensive statistics including total transactions, buy/sell counts, invested amount, and realized gains",
        responses = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<TransactionStats> getTransactionStats(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authToken) {
        
        Long authenticatedUserId = authHelper.getCurrentUserId();
        authHelper.validateUserAccess(authenticatedUserId);
        
        log.info("Fetching transaction statistics for user {}", userId);
        TransactionStats stats = transactionService.getTransactionStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{userId}/date-range")
    @PreAuthorize("hasAnyRole('USER_READ_ONLY', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Get transactions by date range",
        description = "Retrieve transactions within a specific date range",
        responses = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<List<PortfolioTransaction>> getTransactionsByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2024-01-01") LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2024-12-31") LocalDate endDate,
            @RequestHeader("Authorization") String authToken) {
        
        Long authenticatedUserId = authHelper.getCurrentUserId();
        authHelper.validateUserAccess(authenticatedUserId);
        
        log.info("Fetching transactions for user {} between {} and {}", userId, startDate, endDate);
        List<PortfolioTransaction> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{userId}/holdings-summary")
    @PreAuthorize("hasAnyRole('USER_READ_ONLY', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(
        summary = "Get current holdings summary",
        description = "Calculate current holdings quantity and average buy price for each stock based on transactions",
        responses = {
            @ApiResponse(responseCode = "200", description = "Holdings summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<Map<String, Object>> getHoldingsSummary(
            @PathVariable Long userId,
            @RequestParam(required = false) @Parameter(description = "Stock symbol (optional)", example = "RELIANCE") String symbol,
            @RequestHeader("Authorization") String authToken) {
        
        Long authenticatedUserId = authHelper.getCurrentUserId();
        authHelper.validateUserAccess(authenticatedUserId);
        
        if (symbol != null && !symbol.isBlank()) {
            Integer currentHoldings = transactionService.calculateCurrentHoldings(userId, symbol);
            var avgPrice = transactionService.calculateAverageBuyPrice(userId, symbol);
            
            return ResponseEntity.ok(Map.of(
                "symbol", symbol.toUpperCase(),
                "currentQuantity", currentHoldings,
                "averageBuyPrice", avgPrice
            ));
        }
        
        return ResponseEntity.ok(Map.of("message", "Provide a symbol parameter to get holdings details"));
    }
}
