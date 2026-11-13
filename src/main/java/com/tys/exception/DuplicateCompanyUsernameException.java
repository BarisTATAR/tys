package com.tys.exception;

public class DuplicateCompanyUsernameException extends CompanyBusinessException {

    public DuplicateCompanyUsernameException() {
        super("Bu kullanıcı adı zaten kayıtlı.", "DUPLICATE_USERNAME");
    }
}
