package com.tys.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(schema = "tys", name = "guest")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "identityNumber")
    private String identityNumber;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "job")
    private String job;

    @Column(name = "address")
    private String address;

    @Column(name = "country_code")
    private Integer countryCode;

    @Column(name = "plate_number")
    private String plateNumber;

    @Column(name = "short_stay")
    private Boolean shortStay;

    @Column(name = "is_contact")
    private Boolean isContact;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    @Column(name = "check_in_date")
    private LocalDateTime checkInDate;

    @Column(name = "check_out_date")
    private LocalDateTime checkOutDate;

    @ManyToMany(mappedBy = "guests")
    private List<Reservation> reservations = new ArrayList<>();

//    @ManyToOne
//    @JoinColumn(name = "room_id", nullable = false)
//    private Room room;

//    @ManyToOne
//    @JoinColumn(name = "reservation_id", nullable = false)
//    private Reservation reservation;

//    @OneToOne
//    @JoinColumn(name = "reservationContact", nullable = false)
//    private Reservation reservationContact;
}
