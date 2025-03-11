package com.tys.model;

import com.tys.enums.CompanyCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "COMPANY")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "CATEGORY")
    private CompanyCategory category;

    @Column(name = "START_YEAR")
    private Integer startYear;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "TAX_NUMBER")
    private String taxNumber;

    @Column(name = "FACILITY_NUMBER")
    private String facilityNumber;

    @Column(name = "ELECTRIC_CHARGE_STATION")
    private Boolean electricChargeStation;

    @Column(name = "TOTAL_ROOM_NUMBER")
    private Integer totalRoomNumber;

//    // Oda sayısı ve kaçar kişilik oldukları algoritması yapılacak
//    @OneToMany(mappedBy="company")
//    private List<Room> roomList;

}

