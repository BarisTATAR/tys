package com.tys.model;

import com.tys.enums.IsletmeKategorisi;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Entity
@Data
@NoArgsConstructor
@Table(name="Company")
public class Company {

    @Id
    @Column(name = "companyId")
    private Long companyId;

    @Column(name = "companyName")
    private String companyName;

    @Column(name = "companyAddress")
    private String companyAddress;

    @Column(name = "companyCategory")
    private IsletmeKategorisi companyCategory;

    @Column(name = "companyStartYear")
    private int companyStartYear;

    @Column(name = "companyEmail")
    private String companyEmail;

    @Column(name = "companyPhoneNumber")
    private String companyPhoneNumber;

    @Column(name = "companyTaxNumber")
    private String companyTaxNumber;

    @Column(name = "companyFacilityNumber")
    private String companyFacilityNumber;

    @Column(name = "isCompanyElectricChargeStation")
    private Boolean isCompanyElectricChargeStation;

    @Column(name = "companyTotalRoomNumber")
    private int companyTotalRoomNumber;

//    // Oda sayısı ve kaçar kişilik oldukları algoritması yapılacak
//    @OneToMany(mappedBy="company")
//    private List<Room> roomList;


}

