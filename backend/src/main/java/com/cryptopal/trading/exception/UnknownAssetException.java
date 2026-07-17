package com.cryptopal.trading.exception;

public class UnknownAssetException extends RuntimeException {

    public UnknownAssetException(String symbol) {
        super("Unknown asset symbol: " + symbol);
    }
}
