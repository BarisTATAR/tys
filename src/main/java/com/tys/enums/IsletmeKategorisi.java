package com.tys.enums;

public enum IsletmeKategorisi {
        OTEL("Otel"),
        TATIL_KOYU("Tatil Köyü"),
        PANSIYON("Pansiyon"),
        BUNGALOV("Bungalov"),
        APART_OTEL("Apart Otel"),
        MOTEL("Motel"),
        LUX_OTEL("Lüks Otel"),
        BUTIK_OTEL("Butik Otel");

    private final String isletmeKategoriAdi;
    IsletmeKategorisi(String isletmeKategoriAdi) {
        this.isletmeKategoriAdi = isletmeKategoriAdi;
    }

}
