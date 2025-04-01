package com.tys.enums;

public enum ReservationType {


    ODA_KAHVALTI("ODA KAHVALTI"),
    ODA("ODA");

    private final String reservationType;

    ReservationType(String reservationType) {
        this.reservationType = reservationType;
    }
}
