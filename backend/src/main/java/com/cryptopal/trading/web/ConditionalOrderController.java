package com.cryptopal.trading.web;

import com.cryptopal.auth.model.AuthenticatedUser;
import com.cryptopal.trading.dto.ConditionalOrderRequest;
import com.cryptopal.trading.dto.ConditionalOrderView;
import com.cryptopal.trading.service.ConditionalOrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trading/conditional-orders")
public class ConditionalOrderController {

    private final ConditionalOrderService conditionalOrderService;

    public ConditionalOrderController(ConditionalOrderService conditionalOrderService) {
        this.conditionalOrderService = conditionalOrderService;
    }

    @PostMapping
    public ResponseEntity<ConditionalOrderView> create(@AuthenticationPrincipal AuthenticatedUser user,
                                                       @Valid @RequestBody ConditionalOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conditionalOrderService.create(user.userId(), request));
    }

    @GetMapping
    public ResponseEntity<List<ConditionalOrderView>> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(conditionalOrderService.list(user.userId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        conditionalOrderService.cancel(user.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
