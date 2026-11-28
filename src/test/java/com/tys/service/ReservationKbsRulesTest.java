package com.tys.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** KBS'ye girişi bildirilmiş misafirin TCKN değişikliği kuralı. */
class ReservationKbsRulesTest {

    @Test
    void farkliTcknDegisiklikSayilir() {
        assertThat(ReservationService.identityChanged("10000000146", "32318266618")).isTrue();
    }

    @Test
    void ayniTcknBoslukFarkiylaDegisiklikSayilmaz() {
        assertThat(ReservationService.identityChanged("10000000146", " 10000000146 ")).isFalse();
    }

    @Test
    void bosGelenDegerDegisiklikSayilmaz() {
        assertThat(ReservationService.identityChanged("10000000146", null)).isFalse();
        assertThat(ReservationService.identityChanged("10000000146", "  ")).isFalse();
    }

    @Test
    void kayitliTcknYoksaIlkKezGirilebilir() {
        assertThat(ReservationService.identityChanged(null, "10000000146")).isFalse();
        assertThat(ReservationService.identityChanged("", "10000000146")).isFalse();
    }
}
