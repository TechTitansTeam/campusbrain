package com.campusbrain.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {

    private static final long TOKEN_VALIDITY_SECONDS = 24 * 60 * 60; // 24 hours
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, TokenSession> activeTokens = new ConcurrentHashMap<>();

    public static class TokenSession {
        private final String username;
        private final Instant expiryTime;

        public TokenSession(String username, Instant expiryTime) {
            this.username = username;
            this.expiryTime = expiryTime;
        }

        public String getUsername() {
            return username;
        }

        public Instant getExpiryTime() {
            return expiryTime;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiryTime);
        }
    }

    public String createToken(String username) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        Instant expiry = Instant.now().plusSeconds(TOKEN_VALIDITY_SECONDS);
        activeTokens.put(token, new TokenSession(username, expiry));
        return token;
    }

    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        TokenSession session = activeTokens.get(token);
        if (session == null) {
            return false;
        }
        if (session.isExpired()) {
            activeTokens.remove(token);
            return false;
        }
        return true;
    }

    public String getUsernameFromToken(String token) {
        if (!validateToken(token)) {
            return null;
        }
        return activeTokens.get(token).getUsername();
    }

    public void invalidateToken(String token) {
        if (token != null) {
            activeTokens.remove(token);
        }
    }

    public long getTokenValiditySeconds() {
        return TOKEN_VALIDITY_SECONDS;
    }
}
