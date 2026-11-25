package com.tys.request;

import com.tys.enums.CompanyCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyRequest {
    private String username;
    private String password;
    private String adminPassword;
    private String name;
    private String address;
    private String googleCommentsUrl;
    private CompanyCategory category;
    private Integer startYear;
    private String email;
    private String phoneNumber;
    private String taxNumber;
    private String facilityNumber;
    private Boolean electricChargeStation;
    private Integer totalRoomNumber;

    /** İşletmeye ait Jandarma KBS bilgileri (opsiyonel; verilirse üçü birlikte zorunlu). Şifre şifrelenerek saklanır. */
    private String kbsTesisKodu;
    private String kbsKullaniciTc;
    private String kbsSifre;
}
