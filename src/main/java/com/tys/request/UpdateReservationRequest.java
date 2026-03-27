package com.tys.request;

import com.tys.enums.ReservationStatus;
import com.tys.enums.ReservationType;
import com.tys.model.Guest;
import com.tys.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationRequest {

    private Long id;
    private Integer adultGuestNumber;
    private Integer childGuestNumber;
    private Integer babyGuestNumber;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private List<Guest> guests;
    private List<Payment> paymentList;
    private ReservationType reservationType;
    private ReservationStatus reservationStatus;
}
