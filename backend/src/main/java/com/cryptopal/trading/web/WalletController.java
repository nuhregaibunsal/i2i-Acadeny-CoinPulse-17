package com.cryptopal.trading.web;

import com.cryptopal.auth.model.AuthenticatedUser;
import com.cryptopal.trading.dto.DepositRequest;
import com.cryptopal.trading.dto.TransferRequest;
import com.cryptopal.trading.dto.WalletResponse;
import com.cryptopal.trading.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<WalletResponse> deposit(@AuthenticationPrincipal AuthenticatedUser user,
                                                  @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(new WalletResponse(walletService.deposit(user.userId(), request.amount())));
    }

    @PostMapping("/transfer")
    public ResponseEntity<WalletResponse> transfer(@AuthenticationPrincipal AuthenticatedUser user,
                                                   @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(new WalletResponse(
                walletService.transfer(user.userId(), request.toUsername(), request.amount())));
    }
}
