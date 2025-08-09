package com.tys.request;

import com.tys.enums.ReservationStatus;
import com.tys.enums.ReservationType;
import com.tys.model.Guest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReservationRequest {

    private Long id;
    private Integer adultGuestNumber;
    private Integer childGuestNumber;
    private Integer babyGuestNumber;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private List<Guest> guestList;
    private ReservationType reservationType;
    private ReservationStatus reservationStatus;
}
