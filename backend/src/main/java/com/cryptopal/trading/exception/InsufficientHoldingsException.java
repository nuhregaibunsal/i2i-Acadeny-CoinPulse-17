package com.cryptopal.trading.exception;

public class InsufficientHoldingsException extends RuntimeException {

    public InsufficientHoldingsException(String symbol) {
        super("Insufficient " + symbol + " holdings to complete this trade");
    }
}
