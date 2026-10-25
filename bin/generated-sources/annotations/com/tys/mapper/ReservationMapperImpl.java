package com.tys.mapper;

import com.tys.dto.GuestDto;
import com.tys.dto.PaymentDto;
import com.tys.dto.ReservationDto;
import com.tys.model.Guest;
import com.tys.model.Payment;
import com.tys.model.Reservation;
import com.tys.request.CreatePaymentRequest;
import com.tys.request.CreateReservationRequest;
import com.tys.request.UpdateReservationRequest;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-03T19:31:25+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ReservationMapperImpl implements ReservationMapper {

    @Autowired
    private GuestMapper guestMapper;
    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public Reservation createReservationRequestToEntity(CreateReservationRequest createReservationRequest) {
        if ( createReservationRequest == null ) {
            return null;
        }

        Reservation reservation = new Reservation();

        reservation.setAdultGuestNumber( createReservationRequest.getAdultGuestNumber() );
        reservation.setBabyGuestNumber( createReservationRequest.getBabyGuestNumber() );
        reservation.setChildGuestNumber( createReservationRequest.getChildGuestNumber() );
        reservation.setCompany( createReservationRequest.getCompany() );
        reservation.setPaymentList( createPaymentRequestListToPaymentList( createReservationRequest.getPaymentList() ) );
        reservation.setReservationDate( createReservationRequest.getReservationDate() );
        reservation.setReservationMessage( createReservationRequest.getReservationMessage() );
        reservation.setReservationStatus( createReservationRequest.getReservationStatus() );
        reservation.setReservationType( createReservationRequest.getReservationType() );

        reservation.setCheckInDate( com.tys.util.ReservationDateUtils.toStartOfDay(createReservationRequest.getCheckInDate()) );
        reservation.setCheckOutDate( com.tys.util.ReservationDateUtils.toStartOfDay(createReservationRequest.getCheckOutDate()) );

        linkPayment( reservation );

        return reservation;
    }

    @Override
    public void updateExistingReservationWithReservationRequest(UpdateReservationRequest updateReservationRequest, Reservation existingReservation) {
        if ( updateReservationRequest == null ) {
            return;
        }

        if ( updateReservationRequest.getAdultGuestNumber() != null ) {
            existingReservation.setAdultGuestNumber( updateReservationRequest.getAdultGuestNumber() );
        }
        if ( updateReservationRequest.getBabyGuestNumber() != null ) {
            existingReservation.setBabyGuestNumber( updateReservationRequest.getBabyGuestNumber() );
        }
        if ( updateReservationRequest.getChildGuestNumber() != null ) {
            existingReservation.setChildGuestNumber( updateReservationRequest.getChildGuestNumber() );
        }
        if ( updateReservationRequest.getReservationStatus() != null ) {
            existingReservation.setReservationStatus( updateReservationRequest.getReservationStatus() );
        }
        if ( updateReservationRequest.getReservationType() != null ) {
            existingReservation.setReservationType( updateReservationRequest.getReservationType() );
        }

        linkPayment( existingReservation );
    }

    @Override
    public ReservationDto toDto(Reservation reservation) {
        if ( reservation == null ) {
            return null;
        }

        ReservationDto reservationDto = new ReservationDto();

        reservationDto.setGuestList( guestListToGuestDtoList( reservation.getGuests() ) );
        reservationDto.setRoomList( roomsToSimpleRoomList( reservation.getRooms() ) );
        reservationDto.setPaymentList( paymentListToPaymentDtoList( reservation.getPaymentList() ) );
        reservationDto.setAdultGuestNumber( reservation.getAdultGuestNumber() );
        reservationDto.setBabyGuestNumber( reservation.getBabyGuestNumber() );
        reservationDto.setChildGuestNumber( reservation.getChildGuestNumber() );
        reservationDto.setId( reservation.getId() );
        reservationDto.setReservationDate( reservation.getReservationDate() );
        reservationDto.setReservationMessage( reservation.getReservationMessage() );
        reservationDto.setReservationStatus( reservation.getReservationStatus() );
        reservationDto.setReservationType( reservation.getReservationType() );

        reservationDto.setCheckInDate( com.tys.util.ReservationDateUtils.toLocalDate(reservation.getCheckInDate()) );
        reservationDto.setCheckOutDate( com.tys.util.ReservationDateUtils.toLocalDate(reservation.getCheckOutDate()) );

        return reservationDto;
    }

    protected List<Payment> createPaymentRequestListToPaymentList(List<CreatePaymentRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<Payment> list1 = new ArrayList<Payment>( list.size() );
        for ( CreatePaymentRequest createPaymentRequest : list ) {
            list1.add( paymentMapper.createPaymentRequestToEntity( createPaymentRequest ) );
        }

        return list1;
    }

    protected List<GuestDto> guestListToGuestDtoList(List<Guest> list) {
        if ( list == null ) {
            return null;
        }

        List<GuestDto> list1 = new ArrayList<GuestDto>( list.size() );
        for ( Guest guest : list ) {
            list1.add( guestMapper.toDto( guest ) );
        }

        return list1;
    }

    protected List<PaymentDto> paymentListToPaymentDtoList(List<Payment> list) {
        if ( list == null ) {
            return null;
        }

        List<PaymentDto> list1 = new ArrayList<PaymentDto>( list.size() );
        for ( Payment payment : list ) {
            list1.add( paymentMapper.toDto( payment ) );
        }

        return list1;
    }
}
