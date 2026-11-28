package com.tys.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Geri çözülmesi gereken sırlar (ör. KBS şifresi) için AES-256-GCM.
 * Anahtar: app.encryption-key (Base64, 32 bayt). Canlı profilde zorunludur (ProductionReadiness).
 * Geliştirmede tanımlı değilse JWT anahtarından türetilir.
 */
@Slf4j
@Component
public class SecretCipher {

    private static final String PREFIX = "v1:";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public SecretCipher(@Value("${app.encryption-key:}") String encryptionKey,
                        @Value("${jwt.secret:}") String jwtSecret) {
        byte[] raw;
        if (encryptionKey != null && !encryptionKey.isBlank()) {
            try {
                raw = Base64.getDecoder().decode(encryptionKey.trim());
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException("APP_ENCRYPTION_KEY Base64 olmalıdır (32 bayt).");
            }
            if (raw.length != 32) {
                throw new IllegalStateException("APP_ENCRYPTION_KEY 32 bayt (Base64) olmalıdır.");
            }
        } else {
            log.warn("APP_ENCRYPTION_KEY tanımlı değil; şifreleme anahtarı JWT anahtarından türetiliyor (yalnız geliştirme).");
            raw = sha256(("tys-secret-cipher:" + jwtSecret).getBytes(StandardCharsets.UTF_8));
        }
        this.key = new SecretKeySpec(raw, "AES");
    }

    public String encrypt(String plain) {
        if (plain == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            ByteBuffer out = ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted);
            return PREFIX + Base64.getEncoder().encodeToString(out.array());
        } catch (Exception ex) {
            throw new IllegalStateException("Şifreleme yapılamadı.", ex);
        }
    }

    public String decrypt(String stored) {
        if (stored == null) {
            return null;
        }
        if (!stored.startsWith(PREFIX)) {
            throw new IllegalStateException("Şifreli değer biçimi tanınmadı.");
        }
        try {
            byte[] data = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, data, 0, IV_LENGTH));
            byte[] plain = cipher.doFinal(data, IV_LENGTH, data.length - IV_LENGTH);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Şifreli değer çözülemedi (anahtar değişmiş olabilir).", ex);
        }
    }

    public boolean isEncrypted(String stored) {
        return stored != null && stored.startsWith(PREFIX);
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
