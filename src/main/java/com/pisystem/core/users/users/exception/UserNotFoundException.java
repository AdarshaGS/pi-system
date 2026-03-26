package com.users.exception;

import org.springframework.http.HttpStatus;

import com.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User doesn't exist", "USER_NOT_FOUND");
    }

    public UserNotFoundException(String email) {
        super(HttpStatus.NOT_FOUND, "User doesn't exist with email " + email, "USER_NOT_FOUND");
    }

}
