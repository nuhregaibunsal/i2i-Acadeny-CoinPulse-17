package com.cryptopal.trading.web;

import com.cryptopal.auth.model.AuthenticatedUser;
import com.cryptopal.trading.dto.OrderRequest;
import com.cryptopal.trading.dto.OrderResponse;
import com.cryptopal.trading.service.TradingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trading")
public class TradingController {

    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> execute(@AuthenticationPrincipal AuthenticatedUser user,
                                                 @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(tradingService.execute(user.userId(), request));
    }
}
