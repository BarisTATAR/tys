package com.tys.request;
import com.tys.client.SnfEnumKonaklayanKullanimSekli;
import com.tys.client.SnfEnumUlkeler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGuestRequest {
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
    private LocalDate bookingDate;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
