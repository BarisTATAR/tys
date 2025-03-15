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
@Table(schema = "tys", name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "category")
    private CompanyCategory category;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "tax_number")
    private String taxNumber;

    @Column(name = "facility_number")
    private String facilityNumber;

    @Column(name = "electric_charge_station")
    private Boolean electricChargeStation;

    @Column(name = "total_room_number")
    private Integer totalRoomNumber;

//    // Oda sayısı ve kaçar kişilik oldukları algoritması yapılacak
//    @OneToMany(mappedBy="company")
//    private List<Room> roomList;

}

