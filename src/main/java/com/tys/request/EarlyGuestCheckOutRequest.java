package com.tys.request;

import lombok.Data;

@Data
public class EarlyGuestCheckOutRequest {
    private String name;
    private String surname;
    private String identityNumber;
}
