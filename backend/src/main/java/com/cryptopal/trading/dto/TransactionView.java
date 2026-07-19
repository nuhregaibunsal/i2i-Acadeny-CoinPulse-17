package com.cryptopal.trading.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionView(
        String type,
        String symbol,
        BigDecimal volume,
        BigDecimal price,
        BigDecimal totalValue,
        OffsetDateTime createdAt
) {
}
