package com.tys.enums;

public enum SecurityRegion {

    GENDARME("Jandarma"),
    POLICE("Polis");

    private final String securityRegion;

    SecurityRegion(String securityRegion) {
        this.securityRegion = securityRegion;
    }
}
