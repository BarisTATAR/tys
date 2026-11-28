package com.tys.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputRulesTest {

    @Test
    void gucluParolaKabulEdilir() {
        assertThatCode(() -> InputRules.password("Otel2026x", "Şifre")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "abcdefgh", "12345678", "Kisa1", "bosluk var 1a"})
    void zayifParolaReddedilir(String value) {
        assertThatThrownBy(() -> InputRules.password(value, "Şifre")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tcknAlgoritmasi() {
        assertThat(InputRules.isValidTckn("10000000146")).isTrue();
        assertThat(InputRules.isValidTckn("12345678901")).isFalse();
        assertThat(InputRules.isValidTckn("11111111111")).isFalse();
        assertThat(InputRules.isValidTckn("01234567890")).isFalse();
    }

    @Test
    void odaKurallari() {
        assertThatCode(() -> InputRules.room(101, 2, 1)).doesNotThrowAnyException();
        assertThatThrownBy(() -> InputRules.room(null, 2, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InputRules.room(101, -2, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InputRules.room(101, 0, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InputRules.room(101, 2, 500)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ekOdemeTutariPozitifVeTipliOlmali() {
        assertThatCode(() -> InputRules.paidAmount(new BigDecimal("840.00"), "CASH")).doesNotThrowAnyException();
        assertThatThrownBy(() -> InputRules.paidAmount(new BigDecimal("-500"), "CASH")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InputRules.paidAmount(BigDecimal.ZERO, "CASH")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InputRules.paidAmount(null, "CASH")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InputRules.paidAmount(new BigDecimal("10.001"), "CASH")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InputRules.paidAmount(BigDecimal.TEN, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void degerlendirmePuanAraligi() {
        assertThatCode(() -> InputRules.rating(new BigDecimal("4.5"), 5, 4, 3)).doesNotThrowAnyException();
        assertThatThrownBy(() -> InputRules.rating(new BigDecimal("99"), 5)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InputRules.rating(new BigDecimal("4"), -3)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isletmeZorunluAlanlari() {
        assertThatThrownBy(() -> InputRules.totalRoomNumber(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InputRules.totalRoomNumber(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InputRules.facilityNumber(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> InputRules.username("a b")).isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> InputRules.username("otel_a")).doesNotThrowAnyException();
    }

    @Test
    void htmlIcerenMetinReddedilir() {
        assertThatThrownBy(() -> InputRules.plainText("<script>", "Açıklama", 100, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Şükrü", "Jean-Pierre", "O'Brien", "José", "Seán", "Müller", "Anne Marie", "St. John"})
    void yabanciVeTurkceMisafirAdlariKabulEdilir(String name) {
        assertThatCode(() -> InputRules.personName(name, "Misafir adı")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ali2", "<b>", "-Ali", "a@b", "Ali;DROP"})
    void rakamVeIsaretIcerenAdlarReddedilir(String name) {
        assertThatThrownBy(() -> InputRules.personName(name, "Misafir adı"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
