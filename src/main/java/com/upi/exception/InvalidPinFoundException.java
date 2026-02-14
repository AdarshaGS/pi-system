package com.upi.exception;

import org.springframework.http.HttpStatus;
import com.common.exception.BusinessException;

public class InvalidPinFoundException extends BusinessException {
    private String message;

    public InvalidPinFoundException(String message, String code) {
        super(HttpStatus.FORBIDDEN, message, code);
        this.message = message;
    }

    public InvalidPinFoundException(String message) {
        super(HttpStatus.FORBIDDEN, message, "INVALID_PIN_FOUND");
        this.message = message;
    }

    public InvalidPinFoundException() {
        super(HttpStatus.FORBIDDEN, "Invalid PIN provided", "INVALID_PIN_FOUND");
        this.message = "Invalid PIN provided";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
