package com.tys.request;

import com.tys.enums.ReservationStatus;
import com.tys.enums.ReservationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReservationRequest {
    private Long id;
    private Integer totalGuestNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<CreateGuestRequest> guestList;
    private ReservationType reservationType;
    private ReservationStatus reservationState;

}
