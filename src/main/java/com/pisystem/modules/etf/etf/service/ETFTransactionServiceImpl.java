package com.etf.service;

import com.common.security.AuthenticationHelper;
import com.etf.model.ETF;
import com.etf.model.ETFHolding;
import com.etf.model.ETFTransaction;
import com.etf.repository.ETFHoldingRepository;
import com.etf.repository.ETFRepository;
import com.etf.repository.ETFTransactionRepository;
import com.users.data.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class ETFTransactionServiceImpl implements ETFTransactionService {

    @Autowired
    private ETFTransactionRepository transactionRepository;

    @Autowired
    private ETFHoldingRepository holdingRepository;

    @Autowired
    private ETFRepository etfRepository;

    @Autowired
    private AuthenticationHelper authenticationHelper;

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    @Override
    @Transactional
    public ETFTransaction addTransaction(Long userId, ETFTransaction transaction) {
        Users user = authenticationHelper.getUser(userId);
        transaction.setUser(user);

        ETFTransaction saved = transactionRepository.save(transaction);
        updateHolding(transaction);

        return saved;
    }

    @Override
    public List<ETFTransaction> getAllTransactions(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    @Override
    public List<ETFTransaction> getTransactionsByETF(Long userId, Long etfId) {
        return transactionRepository.findByUserIdAndEtfId(userId, etfId);
    }

    @Override
    public List<ETFTransaction> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
    }

    @Override
    @Transactional
    public ETFTransaction updateTransaction(Long userId, Long transactionId, ETFTransaction transaction) {
        ETFTransaction existing = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        reverseHolding(existing);

        existing.setTransactionType(transaction.getTransactionType());
        existing.setTransactionDate(transaction.getTransactionDate());
        existing.setQuantity(transaction.getQuantity());
        existing.setPrice(transaction.getPrice());
        existing.setAmount(transaction.getAmount());
        existing.setBrokerage(transaction.getBrokerage());
        existing.setStt(transaction.getStt());
        existing.setStampDuty(transaction.getStampDuty());
        existing.setTransactionCharges(transaction.getTransactionCharges());
        existing.setGst(transaction.getGst());
        existing.setTotalCharges(transaction.getTotalCharges());
        existing.setNetAmount(transaction.getNetAmount());
        existing.setExchange(transaction.getExchange());
        existing.setOrderId(transaction.getOrderId());
        existing.setNotes(transaction.getNotes());

        ETFTransaction saved = transactionRepository.save(existing);
        updateHolding(saved);

        return saved;
    }

    @Override
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        ETFTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        reverseHolding(transaction);
        transactionRepository.delete(transaction);
    }

    private void updateHolding(ETFTransaction transaction) {
        Long userId = transaction.getUser().getId();
        Long etfId = transaction.getEtf().getId();

        ETFHolding holding = holdingRepository
                .findByUserIdAndEtfId(userId, etfId)
                .orElse(new ETFHolding());

        if (holding.getId() == null) {
            holding.setUser(transaction.getUser());
            holding.setEtf(transaction.getEtf());
        }

        Integer currentQty = holding.getTotalQuantity();
        BigDecimal currentInvested = holding.getInvestedAmount();

        if ("BUY".equals(transaction.getTransactionType())) {
            Integer newQty = currentQty + transaction.getQuantity();
            BigDecimal newInvested = currentInvested.add(transaction.getNetAmount());
            BigDecimal avgPrice = newInvested.divide(BigDecimal.valueOf(newQty), MC);

            holding.setTotalQuantity(newQty);
            holding.setInvestedAmount(newInvested);
            holding.setAveragePrice(avgPrice);
        } else if ("SELL".equals(transaction.getTransactionType())) {
            Integer newQty = currentQty - transaction.getQuantity();
            BigDecimal avgPrice = holding.getAveragePrice();
            BigDecimal newInvested = avgPrice.multiply(BigDecimal.valueOf(newQty));

            holding.setTotalQuantity(newQty);
            holding.setInvestedAmount(newInvested);
        }

        // Update current values
        ETF etf = transaction.getEtf();
        if (etf.getMarketPrice() != null) {
            holding.setCurrentPrice(etf.getMarketPrice());
            BigDecimal currentValue = etf.getMarketPrice().multiply(BigDecimal.valueOf(holding.getTotalQuantity()));
            holding.setCurrentValue(currentValue);

            BigDecimal gain = currentValue.subtract(holding.getInvestedAmount());
            holding.setUnrealizedGain(gain);

            if (holding.getInvestedAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal gainPct = gain.divide(holding.getInvestedAmount(), MC).multiply(BigDecimal.valueOf(100));
                holding.setUnrealizedGainPercentage(gainPct);
            }
        }

        holdingRepository.save(holding);
    }

    private void reverseHolding(ETFTransaction transaction) {
        Long userId = transaction.getUser().getId();
        Long etfId = transaction.getEtf().getId();

        ETFHolding holding = holdingRepository
                .findByUserIdAndEtfId(userId, etfId)
                .orElseThrow(() -> new RuntimeException("Holding not found"));

        Integer currentQty = holding.getTotalQuantity();
        BigDecimal currentInvested = holding.getInvestedAmount();

        if ("BUY".equals(transaction.getTransactionType())) {
            Integer newQty = currentQty - transaction.getQuantity();
            BigDecimal newInvested = currentInvested.subtract(transaction.getNetAmount());

            holding.setTotalQuantity(newQty);
            holding.setInvestedAmount(newInvested);

            if (newQty > 0) {
                BigDecimal avgPrice = newInvested.divide(BigDecimal.valueOf(newQty), MC);
                holding.setAveragePrice(avgPrice);
            }
        } else if ("SELL".equals(transaction.getTransactionType())) {
            Integer newQty = currentQty + transaction.getQuantity();
            BigDecimal avgPrice = holding.getAveragePrice();
            BigDecimal newInvested = avgPrice.multiply(BigDecimal.valueOf(newQty));

            holding.setTotalQuantity(newQty);
            holding.setInvestedAmount(newInvested);
        }

        holdingRepository.save(holding);
    }
}
