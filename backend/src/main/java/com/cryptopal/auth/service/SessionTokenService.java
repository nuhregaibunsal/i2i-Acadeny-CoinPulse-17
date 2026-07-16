package com.cryptopal.auth.service;

import com.cryptopal.auth.model.AuthenticatedUser;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SessionTokenService {

    private static final String KEY_PREFIX = "session:";
    private static final Duration TOKEN_TTL = Duration.ofHours(2);

    private final StringRedisTemplate redisTemplate;

    public SessionTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String issueToken(Long userId, String username) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + token, userId + ":" + username, TOKEN_TTL);
        return token;
    }

    public Optional<AuthenticatedUser> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + token);
        if (value == null) {
            return Optional.empty();
        }
        String[] parts = value.split(":", 2);
        return Optional.of(new AuthenticatedUser(Long.valueOf(parts[0]), parts[1]));
    }

    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            redisTemplate.delete(KEY_PREFIX + token);
        }
    }
}
