package com.tys.mapper;

import com.tys.dto.ReservationDto;
import com.tys.model.Guest;
import com.tys.model.Payment;
import com.tys.model.Reservation;
import com.tys.model.Room;
import com.tys.request.CreateReservationRequest;
import com.tys.request.UpdateReservationRequest;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {GuestMapper.class, RoomMapper.class, PaymentMapper.class})
public interface ReservationMapper {
    Reservation createReservationRequestToEntity(CreateReservationRequest createReservationRequest);

    @AfterMapping
    default void linkPayment(@MappingTarget Reservation reservation) {
        if (reservation.getPaymentList() != null) {
            for (Payment payment : reservation.getPaymentList()) {
                payment.setReservation(reservation);
                payment.setPaymentDate(LocalDate.now());
            }
        }
    }

    void updateExistingReservationWithReservationRequest(UpdateReservationRequest updateReservationRequest, @MappingTarget Reservation existingrReservation);
    @Mapping(source = "guests", target = "guestList")
    ReservationDto toDto(Reservation reservation);

}

