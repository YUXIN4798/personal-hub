package com.tianshi.hub.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AdminSlugService {

    private static final int MAX_SLUG_LENGTH = 64;

    public String normalize(String candidate, String fallbackSource, String prefix) {
        String source = isBlank(candidate) ? fallbackSource : candidate;
        String ascii = Normalizer.normalize(source == null ? "" : source, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (ascii.isBlank()) {
            ascii = trimToLimit(prefix + "-" + shortHash(source), MAX_SLUG_LENGTH);
        }
        return trimToLimit(ascii, MAX_SLUG_LENGTH).replaceAll("-+$", "");
    }

    private String shortHash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 4);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    private String trimToLimit(String value, int limit) {
        return value.length() <= limit ? value : value.substring(0, limit);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
