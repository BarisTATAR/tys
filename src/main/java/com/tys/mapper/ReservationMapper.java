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
    /*@Mapping(source = "adultGuestNumber", target = "adultGuestNumber")
    @Mapping(source = "childGuestNumber", target = "childGuestNumber")
    @Mapping(source = "babyGuestNumber", target = "babyGuestNumber")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "reservationType", target = "reservationType")
    @Mapping(source = "reservationStatus", target = "reservationStatus")
    @Mapping(source = "guestList", target = "guestList")
    @Mapping(source = "roomList", target = "roomList")*/
    Reservation createReservationRequestToEntity(CreateReservationRequest createReservationRequest);

//    @AfterMapping
//    default void linkRooms(@MappingTarget Reservation reservation) {
//        if (reservation.getRoomList() != null) {
//            for (Room room : reservation.getRoomList()) {
//                room.setReservation(reservation);
//                room.setLoaded(true);
//                room.setCompany(reservation.getCompany());
//            }
//        }
//    }

    @AfterMapping
    default void linkPayment(@MappingTarget Reservation reservation) {
        if (reservation.getPaymentList() != null) {
            for (Payment payment : reservation.getPaymentList()) {
                payment.setReservation(reservation);
                payment.setPaymentDate(LocalDate.now());
            }
        }
    }

//    @AfterMapping
//    default void linkGuest(@MappingTarget Reservation reservation) {
//        if (reservation.getGuestList() != null) {
//            for (Guest guest : reservation.getGuestList()) {
//                guest.setReservation(reservation);
//                guest.setCheckInDate((reservation.getCheckInDate()));
//                guest.setCheckOutDate((reservation.getCheckOutDate()));
//                guest.setBookingDate(LocalDateTime.now());
//
//                Room room = new Room();
//                room.setNumber(reservation.getRoomList().get(0).getNumber());
//                guest.setRoom(room);
//            }
//        }
//    }

    //@Mapping(target = "id", ignore = true)
    void updateExistingReservationWithReservationRequest(UpdateReservationRequest updateReservationRequest, @MappingTarget Reservation existingrReservation);

    /*@Mapping(source = "adultGuestNumber", target = "adultGuestNumber")
    @Mapping(source = "childGuestNumber", target = "childGuestNumber")
    @Mapping(source = "babyGuestNumber", target = "babyGuestNumber")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "reservationType", target = "reservationType")
    @Mapping(source = "reservationStatus", target = "reservationStatus")
    @Mapping(source = "roomList", target = "roomList")*/
    ReservationDto toDto(Reservation reservation);

}

