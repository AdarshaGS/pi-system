package com.pisystem.modules.upi.exception;

import org.springframework.http.HttpStatus;

import com.pisystem.shared.exception.BusinessException;

public class PinExistsException extends BusinessException {

    private static final String code = "PIN_EXISTS"; // Example error code

    public PinExistsException() {
        super(HttpStatus.FORBIDDEN, "PIN already exists for this user", code);
    }

}
