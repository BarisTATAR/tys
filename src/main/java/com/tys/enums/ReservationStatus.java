package com.tys.enums;

public enum ReservationStatus {
    RESERVATION_MADE("Rezervasyon Yapıldı."),
    GUEST_INFORMATION_ENTERED("Misafir Bilgileri Girildi."),
    CHECK_IN_DONE("Check-in Yapıldı."),
    CHECK_OUT_DONE("Check-out Yapıldı.");

    private final String reservationStatus;

    ReservationStatus(String reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}
