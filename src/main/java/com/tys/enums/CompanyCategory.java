package com.tys.enums;

import lombok.Getter;

@Getter

public enum CompanyCategory {
        OTEL("Otel"),
        TATIL_KOYU("Tatil Köyü"),
        PANSIYON("Pansiyon"),
        BUNGALOV("Bungalov"),
        APART_OTEL("Apart Otel"),
        MOTEL("Motel"),
        LUX_OTEL("Lüks Otel"),
        BUTIK_OTEL("Butik Otel");

    private final String name;
    CompanyCategory(String name) {
        this.name = name;
    }

}
