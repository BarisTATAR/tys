package com.tys.exception;

public class DuplicateCompanyTssCodeException extends CompanyBusinessException {

    public DuplicateCompanyTssCodeException() {
        super("Bu TSS kodu zaten kayıtlı.", "DUPLICATE_TSS_CODE");
    }
}
