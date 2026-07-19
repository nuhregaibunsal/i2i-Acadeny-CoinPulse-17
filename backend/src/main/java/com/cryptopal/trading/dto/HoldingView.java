package com.cryptopal.trading.dto;

import java.math.BigDecimal;

public record HoldingView(
        String symbol,
        BigDecimal volume,
        BigDecimal currentPrice,
        BigDecimal value,
        BigDecimal avgBuyPrice,
        BigDecimal profitLoss,
        BigDecimal profitLossPct
) {
}
