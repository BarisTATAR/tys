package com.tys.exception;

import lombok.Getter;

@Getter
public abstract class CompanyBusinessException extends RuntimeException {

    private final String errorCode;

    protected CompanyBusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
