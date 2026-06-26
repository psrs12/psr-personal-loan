package com.personalloan.applicationmanagement.application.application;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SSNTokenStore {

    private final Map<String, LocalDateTime> tokenExpiry = new ConcurrentHashMap<>();

    public void store(String token, LocalDateTime expiresAt) {
        tokenExpiry.put(token, expiresAt);
    }

    public boolean isValid(String token) {
        LocalDateTime expiry = tokenExpiry.get(token);
        return expiry != null && LocalDateTime.now().isBefore(expiry);
    }

    public void consume(String token) {
        tokenExpiry.remove(token);
    }
}
