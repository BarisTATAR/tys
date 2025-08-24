package com.tys.dto;

import com.tys.enums.ReservationStatus;
import com.tys.enums.ReservationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class ReservationDto {
    private Long id;
    private Integer adultGuestNumber;
    private Integer childGuestNumber;
    private Integer babyGuestNumber;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private String reservationMessage;
    private ReservationType reservationType;
    private ReservationStatus reservationStatus;
    private LocalDateTime reservationDate;

    private List<GuestDto> guestList;
    private List<RoomDto> roomList;
    private List<PaymentDto> paymentList;
}
