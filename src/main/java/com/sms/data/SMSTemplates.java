package com.sms.data;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SMSTemplates {
    private BigDecimal id;
    private String messageTemplate;
}
