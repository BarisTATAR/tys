package com.tys.request;

import com.tys.enums.CompanyCategory;
import com.tys.model.Kbs;
import com.tys.model.Reservation;
import com.tys.model.Room;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

}
