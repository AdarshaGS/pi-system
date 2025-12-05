package com.stocks.exception;

public class SymbolNotFoundException extends RuntimeException {

    public SymbolNotFoundException(String message) {
        super(message);
    }

}
