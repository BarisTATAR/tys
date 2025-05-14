package com.tys.request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateGuestRequest {
    private Long id;
    private String identityNumber;
    private String name;
    private String surname;
    private String phoneNumber;
    private String email;
    private String job;
    private String address;
    private Integer countryCode;
    private String plateNumber;
    private Boolean shortStay;
    private LocalDate bookingDate;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
