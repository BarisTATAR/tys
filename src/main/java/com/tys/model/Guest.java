package com.tys.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(name="Guest")
public class Guest {

    @Id
    @Column(name = "guestNationalId")
    private Long guestNationalId;

    @Column(name = "guestCountryCode")
    private String guestCountryCode;

//    @ManyToOne
//    @JoinColumn(name="room", nullable=false)
//    private Room room;

    @Column(name = "guestPlateNumber")
    private String guestPlateNumber;

    @Column(name = "guestCheckInDate")
    private LocalDate guestCheckInDate;

    @Column(name = "guestCheckOutDate")
    private LocalDate guestCheckOutDate;

    @Column(name = "guestPhoneNumber")
    private String guestPhoneNumber;

    @Column(name = "guestName")
    private String guestName;

    @Column(name = "guestSurname")
    private String guestSurname;

    @Column(name = "guestJob")
    private String guestJob;

    @Column(name = "isGuestShortStay")
    private boolean isGuestShortStay;

    @Column(name = "guestEmail")
    private String guestEmail;

    @Column(name = "guestBookingDate")
    private LocalDate guestBookingDate;

    @Column(name = "guestAdress")
    private String guestAdress;

}
