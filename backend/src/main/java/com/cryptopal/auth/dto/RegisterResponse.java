package com.cryptopal.auth.dto;

import java.math.BigDecimal;

public record RegisterResponse(String username, BigDecimal startingBalance) {
}
