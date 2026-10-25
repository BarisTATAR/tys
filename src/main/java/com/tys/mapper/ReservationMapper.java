package com.tys.mapper;

import com.tys.dto.ReservationDto;
import com.tys.dto.RoomDto;
import com.tys.model.Payment;
import com.tys.model.Reservation;
import com.tys.model.Room;
import com.tys.request.CreateReservationRequest;
import com.tys.request.UpdateReservationRequest;
import com.tys.util.ReservationDateUtils;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {GuestMapper.class, RoomMapper.class, PaymentMapper.class})
public interface ReservationMapper {

    @Mapping(target = "checkInDate", expression = "java(com.tys.util.ReservationDateUtils.toStartOfDay(createReservationRequest.getCheckInDate()))")
    @Mapping(target = "checkOutDate", expression = "java(com.tys.util.ReservationDateUtils.toStartOfDay(createReservationRequest.getCheckOutDate()))")
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

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    @Mapping(target = "guests", ignore = true)
    @Mapping(target = "paymentList", ignore = true)
    @Mapping(target = "cafeItems", ignore = true)
    @Mapping(target = "reservationDate", ignore = true)
    @Mapping(target = "reservationMessage", ignore = true)
    @Mapping(target = "checkInDate", ignore = true)
    @Mapping(target = "checkOutDate", ignore = true)
    void updateExistingReservationWithReservationRequest(UpdateReservationRequest updateReservationRequest, @MappingTarget Reservation existingReservation);

    @Mapping(source = "guests", target = "guestList")
    @Mapping(source = "rooms", target = "roomList", qualifiedByName = "roomsToSimpleRoomList")
    @Mapping(source = "paymentList", target = "paymentList")
    @Mapping(target = "checkInDate", expression = "java(com.tys.util.ReservationDateUtils.toLocalDate(reservation.getCheckInDate()))")
    @Mapping(target = "checkOutDate", expression = "java(com.tys.util.ReservationDateUtils.toLocalDate(reservation.getCheckOutDate()))")
    ReservationDto toDto(Reservation reservation);

    default LocalDate mapLocalDateTimeToLocalDate(LocalDateTime value) {
        return ReservationDateUtils.toLocalDate(value);
    }

    /**
     * RoomMapper.toDto rezervasyonları da maplemeye çalışınca lazy/circular sorun çıkabiliyor.
     * Oda numarası için yalnızca temel alanları dolduruyoruz.
     */
    @Named("roomsToSimpleRoomList")
    default List<RoomDto> roomsToSimpleRoomList(List<Room> rooms) {
        if (rooms == null) {
            return null;
        }
        return rooms.stream().map(room -> {
            RoomDto dto = new RoomDto();
            dto.setId(room.getId());
            dto.setNumber(room.getNumber());
            dto.setCapacity(room.getCapacity());
            dto.setFloor(room.getFloor());
            dto.setSeaView(room.getSeaView());
            dto.setLoaded(room.getLoaded());
            if (room.getCompany() != null) {
                dto.setCompanyId(room.getCompany().getId());
                dto.setCompanyName(room.getCompany().getName());
            }
            return dto;
        }).toList();
    }
}
