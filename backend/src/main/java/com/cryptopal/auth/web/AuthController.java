package com.cryptopal.auth.web;

import com.cryptopal.auth.dto.ChangePasswordRequest;
import com.cryptopal.auth.dto.LoginRequest;
import com.cryptopal.auth.dto.LoginResponse;
import com.cryptopal.auth.dto.RegisterRequest;
import com.cryptopal.auth.dto.RegisterResponse;
import com.cryptopal.auth.model.AuthenticatedUser;
import com.cryptopal.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUser> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(user);
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AuthenticatedUser user,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user.userId(), request);
        return ResponseEntity.noContent().build();
    }
}
