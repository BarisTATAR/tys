package com.tys.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "GUEST")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "JOB")
    private String job;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "COUNTRY_CODE")
    private Integer countryCode;

    @Column(name = "PLATE_NUMBER")
    private String plateNumber;

    @Column(name = "SHORT_STAY")
    private Boolean shortStay;

    @Column(name = "BOOKING_DATE")
    private LocalDate bookingDate;

    @Column(name = "CHECK_IN_DATE")
    private LocalDate checkInDate;

    @Column(name = "CHECK_OUT_DATE")
    private LocalDate checkOutDate;

//    @ManyToOne
//    @JoinColumn(name="room", nullable=false)
//    private Room room;
}
