package com.cryptopal.ai.web;

import com.cryptopal.ai.dto.AiQueryRequest;
import com.cryptopal.ai.dto.AiQueryResponse;
import com.cryptopal.ai.service.AiInsightService;
import com.cryptopal.auth.model.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiInsightService aiInsightService;

    public AiController(AiInsightService aiInsightService) {
        this.aiInsightService = aiInsightService;
    }

    @PostMapping("/query")
    public ResponseEntity<AiQueryResponse> query(@AuthenticationPrincipal AuthenticatedUser user,
                                                 @Valid @RequestBody AiQueryRequest request) {
        return ResponseEntity.ok(aiInsightService.answer(user.username(), user.userId(), request));
    }
}
