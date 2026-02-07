package com.investments.stocks.service;

import com.investments.stocks.data.RecurringTransaction;
import com.investments.stocks.data.RecurringTransactionHistory;
import com.investments.stocks.dto.RecurringTransactionDTO;
import com.investments.stocks.dto.RecurringTransactionHistoryDTO;
import com.investments.stocks.exception.ResourceNotFoundException;
import com.investments.stocks.repo.RecurringTransactionHistoryRepository;
import com.investments.stocks.repo.RecurringTransactionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service("investmentRecurringTransactionService")
public class InvestmentRecurringTransactionService {
    
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionHistoryRepository historyRepository;
    
    public InvestmentRecurringTransactionService(
            RecurringTransactionRepository recurringTransactionRepository,
            RecurringTransactionHistoryRepository historyRepository) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.historyRepository = historyRepository;
    }
    
    @Transactional
    public RecurringTransactionDTO createRecurringTransaction(RecurringTransactionDTO dto) {
        RecurringTransaction entity = new RecurringTransaction();
        copyDtoToEntity(dto, entity);
        entity = recurringTransactionRepository.save(entity);
        return convertToDto(entity);
    }
    
    @Transactional
    public RecurringTransactionDTO updateRecurringTransaction(Long id, RecurringTransactionDTO dto) {
        RecurringTransaction entity = recurringTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + id));
        
        copyDtoToEntity(dto, entity);
        entity = recurringTransactionRepository.save(entity);
        return convertToDto(entity);
    }
    
    public RecurringTransactionDTO getRecurringTransaction(Long id) {
        RecurringTransaction entity = recurringTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + id));
        return convertToDto(entity);
    }
    
    public List<RecurringTransactionDTO> getUserRecurringTransactions(Long userId) {
        return recurringTransactionRepository.findByUserId(userId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<RecurringTransactionDTO> getActiveRecurringTransactions(Long userId) {
        return recurringTransactionRepository.findByUserIdAndStatus(userId, RecurringTransaction.Status.ACTIVE).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<RecurringTransactionDTO> getRecurringTransactionsByType(Long userId, RecurringTransaction.TransactionType type) {
        return recurringTransactionRepository.findByUserIdAndType(userId, type).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void pauseRecurringTransaction(Long id) {
        RecurringTransaction entity = recurringTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + id));
        entity.setStatus(RecurringTransaction.Status.PAUSED);
        recurringTransactionRepository.save(entity);
    }
    
    @Transactional
    public void resumeRecurringTransaction(Long id) {
        RecurringTransaction entity = recurringTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + id));
        entity.setStatus(RecurringTransaction.Status.ACTIVE);
        entity.calculateNextExecutionDate();
        recurringTransactionRepository.save(entity);
    }
    
    @Transactional
    public void cancelRecurringTransaction(Long id) {
        RecurringTransaction entity = recurringTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + id));
        entity.setStatus(RecurringTransaction.Status.CANCELLED);
        entity.setNextExecutionDate(null);
        recurringTransactionRepository.save(entity);
    }
    
    @Transactional
    public void deleteRecurringTransaction(Long id) {
        RecurringTransaction entity = recurringTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + id));
        recurringTransactionRepository.delete(entity);
    }
    
    @Transactional
    public RecurringTransactionHistoryDTO executeRecurringTransaction(Long id) {
        RecurringTransaction entity = recurringTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id: " + id));
        
        RecurringTransactionHistory history = new RecurringTransactionHistory();
        history.setRecurringTransactionId(id);
        history.setExecutedAt(LocalDateTime.now());
        history.setAmount(entity.getAmount());
        
        try {
            // Here you would integrate with actual transaction creation logic
            // For now, we'll just mark it as success
            history.setStatus(RecurringTransactionHistory.ExecutionStatus.SUCCESS);
            
            entity.markExecuted();
            recurringTransactionRepository.save(entity);
        } catch (Exception e) {
            history.setStatus(RecurringTransactionHistory.ExecutionStatus.FAILED);
            history.setErrorMessage(e.getMessage());
        }
        
        history = historyRepository.save(history);
        
        RecurringTransactionHistoryDTO dto = new RecurringTransactionHistoryDTO();
        BeanUtils.copyProperties(history, dto);
        return dto;
    }
    
    @Transactional
    public void processDueTransactions() {
        List<RecurringTransaction> dueTransactions = 
            recurringTransactionRepository.findTransactionsDueForExecution(LocalDate.now());
        
        for (RecurringTransaction transaction : dueTransactions) {
            try {
                executeRecurringTransaction(transaction.getId());
            } catch (Exception e) {
                // Log error and continue with next transaction
                System.err.println("Failed to execute recurring transaction " + transaction.getId() + ": " + e.getMessage());
            }
        }
    }
    
    public List<RecurringTransactionHistoryDTO> getTransactionHistory(Long recurringTransactionId) {
        return historyRepository.findByRecurringTransactionId(recurringTransactionId).stream()
            .map(history -> {
                RecurringTransactionHistoryDTO dto = new RecurringTransactionHistoryDTO();
                BeanUtils.copyProperties(history, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    public List<RecurringTransactionDTO> getUpcomingTransactions(Long userId, int days) {
        LocalDate endDate = LocalDate.now().plusDays(days);
        return recurringTransactionRepository.findByUserIdAndStatus(userId, RecurringTransaction.Status.ACTIVE).stream()
            .filter(t -> t.getNextExecutionDate() != null && 
                        !t.getNextExecutionDate().isAfter(endDate))
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    private RecurringTransactionDTO convertToDto(RecurringTransaction entity) {
        RecurringTransactionDTO dto = new RecurringTransactionDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // Calculate days until next execution
        if (entity.getNextExecutionDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), entity.getNextExecutionDate());
            dto.setDaysUntilNext(Math.max(0, days));
        }
        
        // Calculate total processed amount
        BigDecimal totalProcessed = entity.getAmount()
            .multiply(BigDecimal.valueOf(entity.getExecutionCount()));
        dto.setTotalProcessed(totalProcessed);
        
        // Load recent history (last 5 executions)
        List<RecurringTransactionHistoryDTO> recentHistory = historyRepository
            .findByRecurringTransactionId(entity.getId()).stream()
            .limit(5)
            .map(history -> {
                RecurringTransactionHistoryDTO historyDto = new RecurringTransactionHistoryDTO();
                BeanUtils.copyProperties(history, historyDto);
                return historyDto;
            })
            .collect(Collectors.toList());
        dto.setRecentHistory(recentHistory);
        
        return dto;
    }
    
    private void copyDtoToEntity(RecurringTransactionDTO dto, RecurringTransaction entity) {
        if (dto.getUserId() != null) entity.setUserId(dto.getUserId());
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getType() != null) entity.setType(dto.getType());
        if (dto.getAmount() != null) entity.setAmount(dto.getAmount());
        if (dto.getCurrency() != null) entity.setCurrency(dto.getCurrency());
        if (dto.getFrequency() != null) entity.setFrequency(dto.getFrequency());
        if (dto.getStartDate() != null) entity.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) entity.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getCategory() != null) entity.setCategory(dto.getCategory());
        if (dto.getSourceAccount() != null) entity.setSourceAccount(dto.getSourceAccount());
        if (dto.getDestinationAccount() != null) entity.setDestinationAccount(dto.getDestinationAccount());
        if (dto.getDayOfMonth() != null) entity.setDayOfMonth(dto.getDayOfMonth());
        if (dto.getDayOfWeek() != null) entity.setDayOfWeek(dto.getDayOfWeek());
        if (dto.getAutoExecute() != null) entity.setAutoExecute(dto.getAutoExecute());
        if (dto.getSendReminder() != null) entity.setSendReminder(dto.getSendReminder());
        if (dto.getReminderDaysBefore() != null) entity.setReminderDaysBefore(dto.getReminderDaysBefore());
        if (dto.getMaxExecutions() != null) entity.setMaxExecutions(dto.getMaxExecutions());
        if (dto.getNotes() != null) entity.setNotes(dto.getNotes());
    }
}
