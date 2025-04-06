package com.tys.enums;

public enum ReservationType {

    ROOM_WITH_BREAKFAST("Oda Kahvaltı"),
    ROOM_ONLY("Oda");

    private final String reservationType;

    ReservationType(String reservationType) {
        this.reservationType = reservationType;
    }
}
