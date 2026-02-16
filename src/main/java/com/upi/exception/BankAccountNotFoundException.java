package com.upi.exception;

import org.springframework.http.HttpStatus;

import com.common.exception.BusinessException;

public class BankAccountNotFoundException extends BusinessException {

    private static final String code = "BANK_ACCOUNT_NOT_FOUND"; // Example error code

    public BankAccountNotFoundException() {
        super(HttpStatus.FORBIDDEN, "Bank account not found.", code);
    }

    // account id 
    public BankAccountNotFoundException(String message) {
        super(HttpStatus.FORBIDDEN, message, code);
    }

}
