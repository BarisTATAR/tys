package com.tys.enums;

public enum ReservationState {
    REZERVASYON_YAPILDI("Rezervasyon Yapıldı."),
    MISAFIR_BILGILERI_GIRILDI("Misafir bilgileri girildi."),
    CHECK_IN_YAPILDI("Check-in Yapıldı."),
    CHECK_OUT_YAPILDI("Check-out Yapıldı.");


    private final String reservationState;

    ReservationState(String reservationState) {
        this.reservationState = reservationState;
    }
}
