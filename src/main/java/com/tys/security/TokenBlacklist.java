package com.tys.security;

import com.tys.model.RevokedToken;
import com.tys.repository.RevokedTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.Optional;

@Component
public class TokenBlacklist {

    private static final ZoneId ZONE = ZoneId.of("Europe/Istanbul");
    private final RevokedTokenRepository revokedTokenRepository;

    public TokenBlacklist(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Transactional
    public void revoke(String token, java.time.Instant expiresAt) {
        if (token == null || token.isBlank()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(ZONE);
        revokedTokenRepository.deleteByExpiresAtBefore(now);
        RevokedToken revoked = new RevokedToken();
        revoked.setTokenHash(hash(token));
        LocalDateTime until = expiresAt == null
                ? now.plusMinutes(30)
                : LocalDateTime.ofInstant(expiresAt, ZONE);
        revoked.setExpiresAt(until);
        revokedTokenRepository.save(revoked);
    }

    @Transactional
    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Optional<RevokedToken> found = revokedTokenRepository.findById(hash(token));
        if (found.isEmpty()) {
            return false;
        }
        if (LocalDateTime.now(ZONE).isAfter(found.get().getExpiresAt())) {
            revokedTokenRepository.delete(found.get());
            return false;
        }
        return true;
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 kullanılamıyor.");
        }
    }
}
