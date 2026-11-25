package com.tys.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretCipherTest {

    private static String key(int fill) {
        byte[] raw = new byte[32];
        java.util.Arrays.fill(raw, (byte) fill);
        return Base64.getEncoder().encodeToString(raw);
    }

    @Test
    void sifreliDegerGeriCozulur() {
        SecretCipher cipher = new SecretCipher(key(7), "");
        String encrypted = cipher.encrypt("KbsSifre!123");
        assertThat(encrypted).startsWith("v1:").doesNotContain("KbsSifre");
        assertThat(cipher.decrypt(encrypted)).isEqualTo("KbsSifre!123");
    }

    @Test
    void ayniDegerHerSeferindeFarkliSifrelenir() {
        SecretCipher cipher = new SecretCipher(key(7), "");
        assertThat(cipher.encrypt("x")).isNotEqualTo(cipher.encrypt("x"));
    }

    @Test
    void farkliAnahtarlaCozulemez() {
        String encrypted = new SecretCipher(key(1), "").encrypt("gizli");
        assertThatThrownBy(() -> new SecretCipher(key(2), "").decrypt(encrypted))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hataliAnahtarUzunluguReddedilir() {
        assertThatThrownBy(() -> new SecretCipher(Base64.getEncoder().encodeToString(new byte[16]), ""))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anahtarYoksaJwtAnahtarindanTuretilir() {
        SecretCipher cipher = new SecretCipher("", "gelistirme-jwt-anahtari-en-az-32-karakter");
        assertThat(cipher.decrypt(cipher.encrypt("deger"))).isEqualTo("deger");
    }
}
