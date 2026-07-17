package com.cryptopal.trading.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
        super("Insufficient funds to complete this trade");
    }
}
