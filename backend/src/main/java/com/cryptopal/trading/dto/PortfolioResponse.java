package com.cryptopal.trading.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponse(BigDecimal cashBalance, BigDecimal holdingsValue, List<HoldingView> holdings) {
}
