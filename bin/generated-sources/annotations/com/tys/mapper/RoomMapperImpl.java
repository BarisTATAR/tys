package com.tys.mapper;

import com.tys.dto.PaymentDto;
import com.tys.dto.ReservationDto;
import com.tys.dto.RoomDto;
import com.tys.model.Company;
import com.tys.model.Payment;
import com.tys.model.Reservation;
import com.tys.model.Room;
import com.tys.request.CreateRoomRequest;
import com.tys.request.UpdateRoomRequest;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T16:20:26+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RoomMapperImpl implements RoomMapper {

    @Override
    public Room createRoomRequestToEntity(CreateRoomRequest createRoomRequest) {
        if ( createRoomRequest == null ) {
            return null;
        }

        Room room = new Room();

        room.setCapacity( createRoomRequest.getCapacity() );
        room.setFloor( createRoomRequest.getFloor() );
        room.setLoaded( createRoomRequest.getLoaded() );
        room.setNumber( createRoomRequest.getNumber() );
        room.setSeaView( createRoomRequest.getSeaView() );

        return room;
    }

    @Override
    public void updateExistingRoomWithRoomRequest(UpdateRoomRequest updateRoomRequest, Room existingRoom) {
        if ( updateRoomRequest == null ) {
            return;
        }

        existingRoom.setCapacity( updateRoomRequest.getCapacity() );
        existingRoom.setFloor( updateRoomRequest.getFloor() );
        existingRoom.setLoaded( updateRoomRequest.getLoaded() );
        existingRoom.setNumber( updateRoomRequest.getNumber() );
        existingRoom.setSeaView( updateRoomRequest.getSeaView() );
    }

    @Override
    public RoomDto toDto(Room room) {
        if ( room == null ) {
            return null;
        }

        RoomDto roomDto = new RoomDto();

        roomDto.setCompanyId( roomCompanyId( room ) );
        roomDto.setCompanyName( roomCompanyName( room ) );
        roomDto.setCapacity( room.getCapacity() );
        roomDto.setFloor( room.getFloor() );
        roomDto.setId( room.getId() );
        roomDto.setLoaded( room.getLoaded() );
        roomDto.setNumber( room.getNumber() );
        roomDto.setReservations( reservationListToReservationDtoList( room.getReservations() ) );
        roomDto.setSeaView( room.getSeaView() );

        return roomDto;
    }

    @Override
    public List<RoomDto> toDtoList(List<Room> rooms) {
        if ( rooms == null ) {
            return null;
        }

        List<RoomDto> list = new ArrayList<RoomDto>( rooms.size() );
        for ( Room room : rooms ) {
            list.add( toDto( room ) );
        }

        return list;
    }

    private Long roomCompanyId(Room room) {
        Company company = room.getCompany();
        if ( company == null ) {
            return null;
        }
        return company.getId();
    }

    private String roomCompanyName(Room room) {
        Company company = room.getCompany();
        if ( company == null ) {
            return null;
        }
        return company.getName();
    }

    protected PaymentDto paymentToPaymentDto(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentDto paymentDto = new PaymentDto();

        paymentDto.setAdvancePayment( payment.getAdvancePayment() );
        paymentDto.setAdvancePaymentType( payment.getAdvancePaymentType() );
        paymentDto.setAmount( payment.getAmount() );
        paymentDto.setClosePaymentType( payment.getClosePaymentType() );
        paymentDto.setId( payment.getId() );
        paymentDto.setPaidAmount( payment.getPaidAmount() );
        paymentDto.setPaymentDate( payment.getPaymentDate() );

        return paymentDto;
    }

    protected List<PaymentDto> paymentListToPaymentDtoList(List<Payment> list) {
        if ( list == null ) {
            return null;
        }

        List<PaymentDto> list1 = new ArrayList<PaymentDto>( list.size() );
        for ( Payment payment : list ) {
            list1.add( paymentToPaymentDto( payment ) );
        }

        return list1;
    }

    protected ReservationDto reservationToReservationDto(Reservation reservation) {
        if ( reservation == null ) {
            return null;
        }

        ReservationDto reservationDto = new ReservationDto();

        reservationDto.setAdultGuestNumber( reservation.getAdultGuestNumber() );
        reservationDto.setBabyGuestNumber( reservation.getBabyGuestNumber() );
        reservationDto.setCheckInDate( reservation.getCheckInDate() );
        reservationDto.setCheckOutDate( reservation.getCheckOutDate() );
        reservationDto.setChildGuestNumber( reservation.getChildGuestNumber() );
        reservationDto.setId( reservation.getId() );
        reservationDto.setPaymentList( paymentListToPaymentDtoList( reservation.getPaymentList() ) );
        reservationDto.setReservationDate( reservation.getReservationDate() );
        reservationDto.setReservationMessage( reservation.getReservationMessage() );
        reservationDto.setReservationStatus( reservation.getReservationStatus() );
        reservationDto.setReservationType( reservation.getReservationType() );

        return reservationDto;
    }

    protected List<ReservationDto> reservationListToReservationDtoList(List<Reservation> list) {
        if ( list == null ) {
            return null;
        }

        List<ReservationDto> list1 = new ArrayList<ReservationDto>( list.size() );
        for ( Reservation reservation : list ) {
            list1.add( reservationToReservationDto( reservation ) );
        }

        return list1;
    }
}
