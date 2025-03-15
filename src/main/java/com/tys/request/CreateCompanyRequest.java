package com.tys.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCompanyRequest {
    private String name;
    private String address;
    private String category;
    private String email;
    private String phoneNumber;
    private String taxNumber;
    private String facilityNumber;
    private Boolean electricChargeStation;
    private Integer totalRoomNumber;
}
