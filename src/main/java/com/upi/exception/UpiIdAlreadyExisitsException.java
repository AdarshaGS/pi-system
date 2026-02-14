package com.upi.exception;

import com.common.exception.BusinessException;

public class UpiIdAlreadyExisitsException extends BusinessException {
    private String message;

    public UpiIdAlreadyExisitsException(String message, String code) {
        super(org.springframework.http.HttpStatus.CONFLICT, message, code);
        this.message = message;
    }

    public UpiIdAlreadyExisitsException(String message) {
        super(org.springframework.http.HttpStatus.CONFLICT, message, "UPI_ID_ALREADY_EXISTS");
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
