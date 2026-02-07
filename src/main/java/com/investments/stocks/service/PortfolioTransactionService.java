package com.investments.stocks.service;

import com.investments.stocks.data.PortfolioTransaction;
import com.investments.stocks.data.PortfolioTransaction.TransactionType;
import com.investments.stocks.dto.PortfolioTransactionRequest;
import com.investments.stocks.dto.TransactionStats;
import com.investments.stocks.repo.PortfolioTransactionRepository;
import com.stocks.monitoring.CustomMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing portfolio transactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioTransactionService {

    private final PortfolioTransactionRepository transactionRepository;
    private final CustomMetrics customMetrics;

    /**
     * Record a new transaction (BUY, SELL, DIVIDEND, etc.)
     */
    @Transactional
    public PortfolioTransaction recordTransaction(PortfolioTransactionRequest request) {
        log.info("Recording transaction: {} {} shares of {} for user {}", 
                request.getTransactionType(), request.getQuantity(), request.getSymbol(), request.getUserId());

        PortfolioTransaction transaction = PortfolioTransaction.builder()
                .userId(request.getUserId())
                .symbol(request.getSymbol().toUpperCase())
                .transactionType(request.getTransactionType())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .fees(request.getFees() != null ? request.getFees() : BigDecimal.ZERO)
                .transactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now())
                .notes(request.getNotes())
                .build();

        // Calculate realized gain for SELL transactions using FIFO
        if (request.getTransactionType() == TransactionType.SELL) {
            BigDecimal realizedGain = calculateRealizedGain(request.getUserId(), request.getSymbol(), 
                    request.getQuantity(), request.getPrice());
            transaction.setRealizedGain(realizedGain);
            log.info("Realized gain for SELL transaction: {}", realizedGain);
        }

        PortfolioTransaction saved = transactionRepository.save(transaction);
        customMetrics.incrementPortfolioTransaction();
        
        return saved;
    }

    /**
     * Calculate realized gain using FIFO (First-In-First-Out) method
     */
    private BigDecimal calculateRealizedGain(Long userId, String symbol, Integer sellQuantity, BigDecimal sellPrice) {
        List<PortfolioTransaction> buyTransactions = transactionRepository
                .findByUserIdAndSymbolAndTransactionTypeOrderByTransactionDateAsc(userId, symbol, TransactionType.BUY);

        BigDecimal totalCost = BigDecimal.ZERO;
        int remainingQuantity = sellQuantity;

        for (PortfolioTransaction buyTx : buyTransactions) {
            if (remainingQuantity <= 0) break;

            int quantityToConsider = Math.min(remainingQuantity, buyTx.getQuantity());
            BigDecimal cost = buyTx.getPrice().multiply(BigDecimal.valueOf(quantityToConsider));
            totalCost = totalCost.add(cost);
            
            remainingQuantity -= quantityToConsider;
        }

        BigDecimal saleAmount = sellPrice.multiply(BigDecimal.valueOf(sellQuantity));
        return saleAmount.subtract(totalCost).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get all transactions for a user
     */
    public List<PortfolioTransaction> getAllTransactions(Long userId) {
        log.info("Fetching all transactions for user {}", userId);
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
    }

    /**
     * Get transactions for a specific symbol
     */
    public List<PortfolioTransaction> getTransactionsBySymbol(Long userId, String symbol) {
        log.info("Fetching transactions for user {} and symbol {}", userId, symbol);
        return transactionRepository.findByUserIdAndSymbolOrderByTransactionDateAsc(userId, symbol.toUpperCase());
    }

    /**
     * Get transaction by ID
     */
    public Optional<PortfolioTransaction> getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    /**
     * Update an existing transaction
     */
    @Transactional
    public PortfolioTransaction updateTransaction(Long transactionId, PortfolioTransactionRequest request) {
        log.info("Updating transaction {}", transactionId);
        
        PortfolioTransaction existing = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + transactionId));

        // Update fields
        existing.setSymbol(request.getSymbol().toUpperCase());
        existing.setTransactionType(request.getTransactionType());
        existing.setQuantity(request.getQuantity());
        existing.setPrice(request.getPrice());
        existing.setFees(request.getFees() != null ? request.getFees() : BigDecimal.ZERO);
        existing.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : existing.getTransactionDate());
        existing.setNotes(request.getNotes());

        // Recalculate realized gain for SELL transactions
        if (request.getTransactionType() == TransactionType.SELL) {
            BigDecimal realizedGain = calculateRealizedGain(existing.getUserId(), request.getSymbol(), 
                    request.getQuantity(), request.getPrice());
            existing.setRealizedGain(realizedGain);
        }

        return transactionRepository.save(existing);
    }

    /**
     * Delete a transaction
     */
    @Transactional
    public void deleteTransaction(Long transactionId) {
        log.info("Deleting transaction {}", transactionId);
        transactionRepository.deleteById(transactionId);
    }

    /**
     * Get transaction statistics for a user
     */
    public TransactionStats getTransactionStats(Long userId) {
        log.info("Calculating transaction statistics for user {}", userId);
        
        List<PortfolioTransaction> allTransactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        
        long totalTransactions = allTransactions.size();
        long buyCount = allTransactions.stream().filter(t -> t.getTransactionType() == TransactionType.BUY).count();
        long sellCount = allTransactions.stream().filter(t -> t.getTransactionType() == TransactionType.SELL).count();
        
        BigDecimal totalInvested = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.BUY)
                .map(PortfolioTransaction::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalRealized = transactionRepository.calculateTotalRealizedGains(userId);

        return TransactionStats.builder()
                .totalTransactions(totalTransactions)
                .buyCount(buyCount)
                .sellCount(sellCount)
                .totalInvested(totalInvested)
                .totalRealizedGains(totalRealized)
                .build();
    }

    /**
     * Get transactions within a date range
     */
    public List<PortfolioTransaction> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching transactions for user {} between {} and {}", userId, startDate, endDate);
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    /**
     * Calculate average buy price for a symbol (for holdings display)
     */
    public BigDecimal calculateAverageBuyPrice(Long userId, String symbol) {
        List<PortfolioTransaction> buyTransactions = transactionRepository
                .findByUserIdAndSymbolAndTransactionTypeOrderByTransactionDateAsc(userId, symbol.toUpperCase(), TransactionType.BUY);

        if (buyTransactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (PortfolioTransaction tx : buyTransactions) {
            totalCost = totalCost.add(tx.getPrice().multiply(BigDecimal.valueOf(tx.getQuantity())));
            totalQuantity += tx.getQuantity();
        }

        return totalQuantity > 0 ? totalCost.divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * Calculate current holdings quantity for a symbol
     */
    public Integer calculateCurrentHoldings(Long userId, String symbol) {
        List<PortfolioTransaction> buyTransactions = transactionRepository
                .findByUserIdAndSymbolAndTransactionTypeOrderByTransactionDateAsc(userId, symbol.toUpperCase(), TransactionType.BUY);
        
        List<PortfolioTransaction> sellTransactions = transactionRepository
                .findByUserIdAndSymbolAndTransactionTypeOrderByTransactionDateAsc(userId, symbol.toUpperCase(), TransactionType.SELL);

        int totalBought = buyTransactions.stream().mapToInt(PortfolioTransaction::getQuantity).sum();
        int totalSold = sellTransactions.stream().mapToInt(PortfolioTransaction::getQuantity).sum();

        return totalBought - totalSold;
    }
}
