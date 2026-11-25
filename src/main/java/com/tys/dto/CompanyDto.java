package com.tys.dto;

import com.tys.enums.CompanyCategory;
import lombok.Data;

@Data
public class CompanyDto {
    private Long id;

    private String username;

    private String name;

    private String address;

    private CompanyCategory category;

    private Integer startYear;

    private String email;

    private String phoneNumber;

    private String taxNumber;

    private String facilityNumber;

    private Boolean electricChargeStation;

    private Integer totalRoomNumber;

    /** KBS tesis bilgileri tanımlı mı (şifre ve kullanıcı TCKN asla dönmez). */
    private Boolean kbsConfigured;

    private String kbsTesisKodu;
}
