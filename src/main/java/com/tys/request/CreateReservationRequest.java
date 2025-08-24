package com.tys.request;

import com.tys.enums.ReservationStatus;
import com.tys.enums.ReservationType;
import com.tys.model.Company;
import com.tys.model.Payment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReservationRequest {
    private Integer adultGuestNumber;
    private Integer childGuestNumber;
    private Integer babyGuestNumber;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private List<CreateGuestRequest> guestList;
    private String reservationMessage;
    private List<CreateRoomRequest> roomList;
    private List<CreatePaymentRequest> paymentList;
    private ReservationType reservationType;
    private ReservationStatus reservationStatus;
    private Company company;
    private LocalDateTime reservationDate;
}
