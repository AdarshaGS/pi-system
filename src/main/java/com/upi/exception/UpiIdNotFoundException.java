package com.upi.exception;

import org.springframework.http.HttpStatus;
import com.common.exception.BusinessException;

public class UpiIdNotFoundException extends BusinessException {

    private static final String code = "UPI_ID_NOT_FOUND";

    public UpiIdNotFoundException() {
        super(HttpStatus.NOT_FOUND, "UPI ID not found.", code);
    }

    public UpiIdNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message, code);
    }
}