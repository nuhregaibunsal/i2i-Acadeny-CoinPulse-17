package com.cryptopal.trading.dto;

import com.cryptopal.trading.model.OrderSide;
import java.math.BigDecimal;

public record OrderResponse(
        String symbol,
        OrderSide side,
        BigDecimal volume,
        BigDecimal price,
        BigDecimal totalValue,
        BigDecimal cashBalance
) {
}
