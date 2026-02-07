package com.mutualfund.service;

import com.common.security.AuthenticationHelper;
import com.mutualfund.model.MutualFund;
import com.mutualfund.model.MutualFundHolding;
import com.mutualfund.model.MutualFundTransaction;
import com.mutualfund.repository.MutualFundHoldingRepository;
import com.mutualfund.repository.MutualFundRepository;
import com.mutualfund.repository.MutualFundTransactionRepository;
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
public class MutualFundTransactionServiceImpl implements MutualFundTransactionService {
    
    @Autowired
    private MutualFundTransactionRepository transactionRepository;
    
    @Autowired
    private MutualFundHoldingRepository holdingRepository;
    
    @Autowired
    private MutualFundRepository mutualFundRepository;
    
    @Autowired
    private AuthenticationHelper authenticationHelper;
    
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);
    
    @Override
    @Transactional
    public MutualFundTransaction addTransaction(Long userId, MutualFundTransaction transaction) {
        Users user = authenticationHelper.getUser(userId);
        transaction.setUser(user);
        
        MutualFundTransaction saved = transactionRepository.save(transaction);
        updateHolding(transaction);
        
        return saved;
    }
    
    @Override
    public List<MutualFundTransaction> getAllTransactions(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
    
    @Override
    public List<MutualFundTransaction> getTransactionsByFund(Long userId, Long mutualFundId) {
        return transactionRepository.findByUserIdAndMutualFundId(userId, mutualFundId);
    }
    
    @Override
    public List<MutualFundTransaction> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
    }
    
    @Override
    @Transactional
    public MutualFundTransaction updateTransaction(Long userId, Long transactionId, MutualFundTransaction transaction) {
        MutualFundTransaction existing = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        if (!existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        // Reverse old holding
        reverseHolding(existing);
        
        // Update transaction
        existing.setTransactionType(transaction.getTransactionType());
        existing.setTransactionDate(transaction.getTransactionDate());
        existing.setUnits(transaction.getUnits());
        existing.setNav(transaction.getNav());
        existing.setAmount(transaction.getAmount());
        existing.setStampDuty(transaction.getStampDuty());
        existing.setTransactionCharges(transaction.getTransactionCharges());
        existing.setStt(transaction.getStt());
        existing.setFolioNumber(transaction.getFolioNumber());
        existing.setNotes(transaction.getNotes());
        
        MutualFundTransaction saved = transactionRepository.save(existing);
        updateHolding(saved);
        
        return saved;
    }
    
    @Override
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        MutualFundTransaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        reverseHolding(transaction);
        transactionRepository.delete(transaction);
    }
    
    private void updateHolding(MutualFundTransaction transaction) {
        Long userId = transaction.getUser().getId();
        Long fundId = transaction.getMutualFund().getId();
        String folio = transaction.getFolioNumber();
        
        MutualFundHolding holding = holdingRepository
            .findByUserIdAndMutualFundIdAndFolioNumber(userId, fundId, folio)
            .orElse(new MutualFundHolding());
        
        if (holding.getId() == null) {
            holding.setUser(transaction.getUser());
            holding.setMutualFund(transaction.getMutualFund());
            holding.setFolioNumber(folio);
        }
        
        BigDecimal currentUnits = holding.getTotalUnits();
        BigDecimal currentInvested = holding.getInvestedAmount();
        
        if ("BUY".equals(transaction.getTransactionType()) || "SIP".equals(transaction.getTransactionType())) {
            BigDecimal newUnits = currentUnits.add(transaction.getUnits());
            BigDecimal newInvested = currentInvested.add(transaction.getAmount());
            BigDecimal avgNav = newInvested.divide(newUnits, MC);
            
            holding.setTotalUnits(newUnits);
            holding.setInvestedAmount(newInvested);
            holding.setAverageNav(avgNav);
        } else if ("SELL".equals(transaction.getTransactionType())) {
            BigDecimal newUnits = currentUnits.subtract(transaction.getUnits());
            BigDecimal avgNav = holding.getAverageNav();
            BigDecimal newInvested = newUnits.multiply(avgNav);
            
            holding.setTotalUnits(newUnits);
            holding.setInvestedAmount(newInvested);
        }
        
        // Update current values
        MutualFund fund = transaction.getMutualFund();
        if (fund.getNav() != null) {
            holding.setCurrentNav(fund.getNav());
            BigDecimal currentValue = holding.getTotalUnits().multiply(fund.getNav());
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
    
    private void reverseHolding(MutualFundTransaction transaction) {
        // Similar logic but reverse the transaction
        Long userId = transaction.getUser().getId();
        Long fundId = transaction.getMutualFund().getId();
        String folio = transaction.getFolioNumber();
        
        MutualFundHolding holding = holdingRepository
            .findByUserIdAndMutualFundIdAndFolioNumber(userId, fundId, folio)
            .orElseThrow(() -> new RuntimeException("Holding not found"));
        
        BigDecimal currentUnits = holding.getTotalUnits();
        BigDecimal currentInvested = holding.getInvestedAmount();
        
        if ("BUY".equals(transaction.getTransactionType()) || "SIP".equals(transaction.getTransactionType())) {
            // Reverse buy: subtract units and amount
            BigDecimal newUnits = currentUnits.subtract(transaction.getUnits());
            BigDecimal newInvested = currentInvested.subtract(transaction.getAmount());
            
            holding.setTotalUnits(newUnits);
            holding.setInvestedAmount(newInvested);
            
            if (newUnits.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal avgNav = newInvested.divide(newUnits, MC);
                holding.setAverageNav(avgNav);
            }
        } else if ("SELL".equals(transaction.getTransactionType())) {
            // Reverse sell: add units back
            BigDecimal newUnits = currentUnits.add(transaction.getUnits());
            BigDecimal avgNav = holding.getAverageNav();
            BigDecimal newInvested = newUnits.multiply(avgNav);
            
            holding.setTotalUnits(newUnits);
            holding.setInvestedAmount(newInvested);
        }
        
        holdingRepository.save(holding);
    }
}
