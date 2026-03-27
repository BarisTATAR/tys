package com.tys.model;

import com.tys.enums.ReservationStatus;
import com.tys.enums.ReservationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "tys", name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "reservation_date")    //Reservation date
    private LocalDateTime reservationDate;

    @Column(name = "adult_guest_number")   // Total guest number
    private Integer adultGuestNumber;

    @Column(name = "child_guest_number")   // Total guest number
    private Integer childGuestNumber;

    @Column(name = "baby_guest_number")   // Total guest number
    private Integer babyGuestNumber;

    @Column(name = "reservation_message")   // Total guest number
    private String reservationMessage;

    @Column(name = "check_in_date")    //Reservation start date
    private LocalDateTime checkInDate;

    @Column(name = "check_out_date")      //Reservation end date
    private LocalDateTime checkOutDate;

    @Column(name = "reservation_type")      //Reservation type
    private ReservationType reservationType;

    @Column(name = "reservation_status")      //Reservation end date
    private ReservationStatus reservationStatus;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToMany
    @JoinTable(
            name = "reservation_room",
            schema = "tys",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "room_id")
    )
    private List<Room> rooms;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "reservation_guest",
            schema = "tys",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "guest_id")
    )
    private List<Guest> guests = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<Payment> paymentList;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationCafeItem> cafeItems = new ArrayList<>();
}