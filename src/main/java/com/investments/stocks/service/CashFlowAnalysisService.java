package com.investments.stocks.service;

import com.investments.stocks.data.CashFlowRecord;
import com.investments.stocks.repo.CashFlowRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CashFlowAnalysisService {
    
    private final CashFlowRecordRepository cashFlowRecordRepository;
    
    public CashFlowAnalysisService(CashFlowRecordRepository cashFlowRecordRepository) {
        this.cashFlowRecordRepository = cashFlowRecordRepository;
    }
    
    public Map<String, Object> getCashFlowSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        List<CashFlowRecord> records = cashFlowRecordRepository.findByDateRange(userId, startDate, endDate);
        
        BigDecimal totalIncome = records.stream()
            .map(CashFlowRecord::getTotalIncome)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpenses = records.stream()
            .map(CashFlowRecord::getTotalExpenses)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);
        
        BigDecimal averageIncome = records.isEmpty() ? BigDecimal.ZERO :
            totalIncome.divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal averageExpenses = records.isEmpty() ? BigDecimal.ZERO :
            totalExpenses.divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("netCashFlow", netCashFlow);
        summary.put("averageIncome", averageIncome);
        summary.put("averageExpenses", averageExpenses);
        summary.put("savingsRate", calculateSavingsRate(totalIncome, totalExpenses));
        summary.put("records", records);
        
        return summary;
    }
    
    public Map<String, Object> getProjections(Long userId, int months) {
        List<CashFlowRecord> historicalData = cashFlowRecordRepository.findRecentRecords(userId);
        
        if (historicalData.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Calculate averages from historical data
        BigDecimal avgIncome = historicalData.stream()
            .map(CashFlowRecord::getTotalIncome)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(historicalData.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal avgExpenses = historicalData.stream()
            .map(CashFlowRecord::getTotalExpenses)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(historicalData.size()), 2, RoundingMode.HALF_UP);
        
        // Project future cash flow
        List<Map<String, Object>> projections = new ArrayList<>();
        BigDecimal cumulativeCashFlow = BigDecimal.ZERO;
        
        for (int i = 1; i <= months; i++) {
            Map<String, Object> projection = new HashMap<>();
            LocalDate projectionDate = LocalDate.now().plusMonths(i);
            
            BigDecimal projectedIncome = avgIncome;
            BigDecimal projectedExpenses = avgExpenses;
            BigDecimal netFlow = projectedIncome.subtract(projectedExpenses);
            cumulativeCashFlow = cumulativeCashFlow.add(netFlow);
            
            projection.put("month", projectionDate);
            projection.put("projectedIncome", projectedIncome);
            projection.put("projectedExpenses", projectedExpenses);
            projection.put("netCashFlow", netFlow);
            projection.put("cumulativeCashFlow", cumulativeCashFlow);
            
            projections.add(projection);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("projections", projections);
        result.put("baseIncome", avgIncome);
        result.put("baseExpenses", avgExpenses);
        result.put("finalCumulativeCashFlow", cumulativeCashFlow);
        
        return result;
    }
    
    public Map<String, BigDecimal> getCategoryBreakdown(Long userId, LocalDate startDate, LocalDate endDate) {
        List<CashFlowRecord> records = cashFlowRecordRepository.findByDateRange(userId, startDate, endDate);
        
        Map<String, BigDecimal> breakdown = new HashMap<>();
        breakdown.put("housing", records.stream().map(CashFlowRecord::getHousingExpenses).reduce(BigDecimal.ZERO, BigDecimal::add));
        breakdown.put("transportation", records.stream().map(CashFlowRecord::getTransportationExpenses).reduce(BigDecimal.ZERO, BigDecimal::add));
        breakdown.put("food", records.stream().map(CashFlowRecord::getFoodExpenses).reduce(BigDecimal.ZERO, BigDecimal::add));
        breakdown.put("utilities", records.stream().map(CashFlowRecord::getUtilitiesExpenses).reduce(BigDecimal.ZERO, BigDecimal::add));
        breakdown.put("entertainment", records.stream().map(CashFlowRecord::getEntertainmentExpenses).reduce(BigDecimal.ZERO, BigDecimal::add));
        breakdown.put("healthcare", records.stream().map(CashFlowRecord::getHealthcareExpenses).reduce(BigDecimal.ZERO, BigDecimal::add));
        breakdown.put("education", records.stream().map(CashFlowRecord::getEducationExpenses).reduce(BigDecimal.ZERO, BigDecimal::add));
        breakdown.put("debtPayments", records.stream().map(CashFlowRecord::getDebtPayments).reduce(BigDecimal.ZERO, BigDecimal::add));
        breakdown.put("other", records.stream().map(CashFlowRecord::getOtherExpenses).reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return breakdown;
    }
    
    private BigDecimal calculateSavingsRate(BigDecimal income, BigDecimal expenses) {
        if (income.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.subtract(expenses)
            .divide(income, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}
