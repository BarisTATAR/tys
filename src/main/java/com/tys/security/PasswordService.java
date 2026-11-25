package com.tys.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Parolalar yalnız BCrypt ile saklanır ve doğrulanır. Düz metin parola kabul edilmez;
 * eski düz metin kayıtlar açılışta {@code PasswordHashMigration} ile bir kez hash'e çevrilir.
 */
@Service
public class PasswordService {

    /** Var olmayan kullanıcıda yanıt süresini eşitlemek için sabit bir BCrypt hash'i. */
    private final String dummyHash;
    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("tys-zamanlama-esitleme");
    }

    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean isHashed(String storedPassword) {
        return storedPassword != null
                && (storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$"));
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null || !isHashed(storedPassword)) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, storedPassword);
    }

    /** Kullanıcı bulunamadığında da bir BCrypt karşılaştırması yaparak kullanıcı adı tahminini zorlaştırır. */
    public void burnTime() {
        passwordEncoder.matches("tys-olmayan-kullanici", dummyHash);
    }
}
