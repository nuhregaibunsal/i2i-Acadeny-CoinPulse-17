package com.cryptopal.market.model;

import java.math.BigDecimal;

public record CryptoPrice(String symbol, BigDecimal price) {
}
