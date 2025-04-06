package com.tys.enums;

public enum PaymentType {

    CASH("Nakit"),
    CREDIT_CARD("Kredi Kartı"),
    DEBIT_CARD("Banka Kartı");

    private final String paymentType;


    PaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}
