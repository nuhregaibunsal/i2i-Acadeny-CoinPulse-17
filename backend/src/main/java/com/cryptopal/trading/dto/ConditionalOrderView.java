package com.cryptopal.trading.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ConditionalOrderView(
        Long id,
        String symbol,
        String side,
        String direction,
        BigDecimal targetPrice,
        BigDecimal volume,
        String status,
        OffsetDateTime createdAt
) {
}
