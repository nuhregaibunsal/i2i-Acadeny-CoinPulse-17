package com.cryptopal.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cryptopal.auth.dto.LoginRequest;
import com.cryptopal.auth.dto.LoginResponse;
import com.cryptopal.auth.dto.RegisterRequest;
import com.cryptopal.auth.dto.RegisterResponse;
import com.cryptopal.auth.entity.User;
import com.cryptopal.auth.exception.InvalidCredentialsException;
import com.cryptopal.auth.exception.UsernameAlreadyExistsException;
import com.cryptopal.auth.repository.UserRepository;
import com.cryptopal.auth.repository.WalletRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private UserRepository userRepository;
    private WalletRepository walletRepository;
    private PasswordEncoder passwordEncoder;
    private SessionTokenService sessionTokenService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        walletRepository = Mockito.mock(WalletRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        sessionTokenService = Mockito.mock(SessionTokenService.class);
        authService = new AuthService(userRepository, walletRepository, passwordEncoder, sessionTokenService);
    }

    @Test
    void register_persistsUserAndReturnsBalanceInRange() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        User saved = Mockito.mock(User.class);
        when(saved.getId()).thenReturn(1L);
        when(saved.getUsername()).thenReturn("alice");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        RegisterResponse response = authService.register(new RegisterRequest("alice", "secret123"));

        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.startingBalance().doubleValue()).isBetween(10000.0, 50000.0);
    }

    @Test
    void register_throwsWhenUsernameTaken() {
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("bob", "secret123")))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void login_returnsToken_whenCredentialsValid() {
        User user = Mockito.mock(User.class);
        when(user.getId()).thenReturn(5L);
        when(user.getUsername()).thenReturn("carol");
        when(user.getPasswordHash()).thenReturn("hashed");
        when(userRepository.findByUsername("carol")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(sessionTokenService.issueToken(5L, "carol")).thenReturn("token-abc");

        LoginResponse response = authService.login(new LoginRequest("carol", "secret123"));

        assertThat(response.token()).isEqualTo("token-abc");
        assertThat(response.username()).isEqualTo("carol");
    }

    @Test
    void login_throwsWhenPasswordWrong() {
        User user = Mockito.mock(User.class);
        when(user.getPasswordHash()).thenReturn("hashed");
        when(userRepository.findByUsername("dave")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("dave", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throwsWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
