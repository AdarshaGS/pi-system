package com.pisystem.modules.sms.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.pisystem.modules.sms.data.SMSTransaction.ParseStatus;
import com.pisystem.modules.sms.data.SMSTransaction.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to hold parsed SMS data before conversion to entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedSMSData {
    
    private BigDecimal amount;
    private LocalDate transactionDate;
    private LocalTime transactionTime;
    private TransactionType transactionType;
    private String merchant;
    private String accountNumber;
    private String cardNumber;
    private BigDecimal balance;
    private String referenceNumber;
    private String upiId;
    private ParseStatus parseStatus;
    
    @Builder.Default
    private Double confidence = 0.0;
    
    private String errorMessage;
    
    private String category; 
    
    @Builder.Default
    private List<String> tags = new ArrayList<>(); 
    
    private Boolean isRecurring; 
    
    private Boolean dateFromMessage; 
    
    
    private String fromAccount; 
    private String toAccount; 
}
