package com.cryptopal.market.dto;

import java.math.BigDecimal;

public record PricePoint(BigDecimal price, long time) {
}
