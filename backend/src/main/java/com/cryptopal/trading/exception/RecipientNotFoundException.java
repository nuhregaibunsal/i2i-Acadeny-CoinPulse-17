package com.cryptopal.trading.exception;

public class RecipientNotFoundException extends RuntimeException {

    public RecipientNotFoundException(String username) {
        super("No user found with username '" + username + "'");
    }
}
