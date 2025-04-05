package com.tys.model;

import com.tys.enums.ReservationStatus;
import com.tys.enums.ReservationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(schema = "tys", name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "total_guest_number")   // Total guest number
    private Integer totalGuestNumber;

    @Column(name = "start_date")    //Reservation start date
    private LocalDateTime startDate;

    @Column(name = "end_date")      //Reservation end date
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "reservation")
    private List<Guest> guestList;

    @Column(name = "reservation_type")      //Reservation type
    private ReservationType reservationType;

    @Column(name = "reservationState")      //Reservation end date
    private ReservationStatus reservationStatus;

}