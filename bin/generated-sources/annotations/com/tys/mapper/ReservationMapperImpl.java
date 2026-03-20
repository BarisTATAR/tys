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
    date = "2026-03-20T16:20:26+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
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
        reservation.setCheckInDate( createReservationRequest.getCheckInDate() );
        reservation.setCheckOutDate( createReservationRequest.getCheckOutDate() );
        reservation.setChildGuestNumber( createReservationRequest.getChildGuestNumber() );
        reservation.setCompany( createReservationRequest.getCompany() );
        reservation.setPaymentList( createPaymentRequestListToPaymentList( createReservationRequest.getPaymentList() ) );
        reservation.setReservationDate( createReservationRequest.getReservationDate() );
        reservation.setReservationMessage( createReservationRequest.getReservationMessage() );
        reservation.setReservationStatus( createReservationRequest.getReservationStatus() );
        reservation.setReservationType( createReservationRequest.getReservationType() );

        linkPayment( reservation );

        return reservation;
    }

    @Override
    public void updateExistingReservationWithReservationRequest(UpdateReservationRequest updateReservationRequest, Reservation existingrReservation) {
        if ( updateReservationRequest == null ) {
            return;
        }

        existingrReservation.setAdultGuestNumber( updateReservationRequest.getAdultGuestNumber() );
        existingrReservation.setBabyGuestNumber( updateReservationRequest.getBabyGuestNumber() );
        existingrReservation.setCheckInDate( updateReservationRequest.getCheckInDate() );
        existingrReservation.setCheckOutDate( updateReservationRequest.getCheckOutDate() );
        existingrReservation.setChildGuestNumber( updateReservationRequest.getChildGuestNumber() );
        if ( existingrReservation.getGuests() != null ) {
            List<Guest> list = updateReservationRequest.getGuests();
            if ( list != null ) {
                existingrReservation.getGuests().clear();
                existingrReservation.getGuests().addAll( list );
            }
            else {
                existingrReservation.setGuests( null );
            }
        }
        else {
            List<Guest> list = updateReservationRequest.getGuests();
            if ( list != null ) {
                existingrReservation.setGuests( new ArrayList<Guest>( list ) );
            }
        }
        existingrReservation.setId( updateReservationRequest.getId() );
        if ( existingrReservation.getPaymentList() != null ) {
            List<Payment> list1 = updateReservationRequest.getPaymentList();
            if ( list1 != null ) {
                existingrReservation.getPaymentList().clear();
                existingrReservation.getPaymentList().addAll( list1 );
            }
            else {
                existingrReservation.setPaymentList( null );
            }
        }
        else {
            List<Payment> list1 = updateReservationRequest.getPaymentList();
            if ( list1 != null ) {
                existingrReservation.setPaymentList( new ArrayList<Payment>( list1 ) );
            }
        }
        existingrReservation.setReservationStatus( updateReservationRequest.getReservationStatus() );
        existingrReservation.setReservationType( updateReservationRequest.getReservationType() );

        linkPayment( existingrReservation );
    }

    @Override
    public ReservationDto toDto(Reservation reservation) {
        if ( reservation == null ) {
            return null;
        }

        ReservationDto reservationDto = new ReservationDto();

        reservationDto.setGuestList( guestListToGuestDtoList( reservation.getGuests() ) );
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
