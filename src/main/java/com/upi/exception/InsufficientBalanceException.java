package com.upi.exception;

import org.springframework.http.HttpStatus;
import com.common.exception.BusinessException;

public class InsufficientBalanceException extends BusinessException {

    private static final String code = "INSUFFICIENT_BALANCE";

    public InsufficientBalanceException() {
        super(HttpStatus.BAD_REQUEST, "Insufficient balance.", code);
    }

    public InsufficientBalanceException(String message) {
        super(HttpStatus.BAD_REQUEST, message, code);
    }
}