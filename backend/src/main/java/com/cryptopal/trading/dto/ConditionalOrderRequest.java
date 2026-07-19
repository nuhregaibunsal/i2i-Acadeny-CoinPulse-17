package com.cryptopal.trading.dto;

import com.cryptopal.trading.model.OrderSide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ConditionalOrderRequest(

        @NotBlank(message = "symbol is required")
        String symbol,

        @NotNull(message = "side is required")
        OrderSide side,

        @NotNull(message = "targetPrice is required")
        @Positive(message = "targetPrice must be greater than zero")
        BigDecimal targetPrice,

        @NotNull(message = "volume is required")
        @Positive(message = "volume must be greater than zero")
        BigDecimal volume
) {
}
