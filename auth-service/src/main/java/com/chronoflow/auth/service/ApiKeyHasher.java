package com.chronoflow.auth.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class ApiKeyHasher {

    private static final String KEY_PREFIX = "cfk_";
    private final SecureRandom secureRandom = new SecureRandom();

    public GeneratedKey generate() {
        byte[] raw = new byte[32];
        secureRandom.nextBytes(raw);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        String apiKey = KEY_PREFIX + secret;
        return new GeneratedKey(apiKey, hash(apiKey), KEY_PREFIX + secret.substring(0, 8));
    }

    public String hash(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record GeneratedKey(String apiKey, String keyHash, String keyPrefix) {
    }
}
