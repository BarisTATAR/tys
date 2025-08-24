package com.tys.dto;

import com.tys.client.SnfEnumKonaklayanKullanimSekli;
import com.tys.client.SnfEnumUlkeler;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuestDto {
    private Long id;
    private String identityNumber;
    private String name;
    private String surname;
    private String phoneNumber;
    private String email;
    private String job;
    private String address;
    private SnfEnumUlkeler countryCode;
    private SnfEnumKonaklayanKullanimSekli guestUsageType;
    private String plateNumber;
    private Boolean shortStay;
    private Boolean isContact;
    private LocalDate bookingDate;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long roomId;
    private Long reservationId;
    private Long reservationContactId;
}
