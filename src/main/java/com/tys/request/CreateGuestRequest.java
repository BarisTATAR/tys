package com.tys.request;


import com.tys.client.SnfEnumKonaklayanKullanimSekli;
import com.tys.client.SnfEnumUlkeler;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGuestRequest {
    private String identityNumber;
    @NotEmpty(message = "Misafir adı boş olamaz!")
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
    private LocalDateTime bookingDate;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private Long roomId;
    private Long reservationId;

}
