package com.cryptopal.auth.service;

import com.cryptopal.auth.dto.ChangePasswordRequest;
import com.cryptopal.auth.dto.LoginRequest;
import com.cryptopal.auth.dto.LoginResponse;
import com.cryptopal.auth.dto.RegisterRequest;
import com.cryptopal.auth.dto.RegisterResponse;
import com.cryptopal.auth.entity.User;
import com.cryptopal.auth.entity.Wallet;
import com.cryptopal.auth.exception.InvalidCredentialsException;
import com.cryptopal.auth.exception.UsernameAlreadyExistsException;
import com.cryptopal.auth.repository.UserRepository;
import com.cryptopal.auth.repository.WalletRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final BigDecimal MIN_STARTING_BALANCE = new BigDecimal("10000");
    private static final BigDecimal MAX_STARTING_BALANCE = new BigDecimal("50000");

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionTokenService sessionTokenService;

    public AuthService(UserRepository userRepository,
                       WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder,
                       SessionTokenService sessionTokenService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenService = sessionTokenService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        User user = new User(request.username(), passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);

        BigDecimal startingBalance = randomStartingBalance();
        walletRepository.save(new Wallet(savedUser.getId(), startingBalance));

        return new RegisterResponse(savedUser.getUsername(), startingBalance);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = sessionTokenService.issueToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getUsername());
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private BigDecimal randomStartingBalance() {
        double amount = ThreadLocalRandom.current()
                .nextDouble(MIN_STARTING_BALANCE.doubleValue(), MAX_STARTING_BALANCE.doubleValue());
        return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
    }
}
