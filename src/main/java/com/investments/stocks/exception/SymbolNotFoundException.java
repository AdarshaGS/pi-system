package com.investments.stocks.exception;

public class SymbolNotFoundException extends RuntimeException {

    public SymbolNotFoundException(String message) {
        super(message);
    }

}
