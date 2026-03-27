package com.tys.mapper;

import com.tys.dto.GuestDto;
import com.tys.model.Guest;
import com.tys.request.CreateGuestRequest;
import com.tys.request.UpdateGuestRequest;
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
public class GuestMapperImpl implements GuestMapper {

    @Override
    public Guest createGuestRequestToEntity(CreateGuestRequest createGuestRequest) {
        if ( createGuestRequest == null ) {
            return null;
        }

        Guest guest = new Guest();

        guest.setAddress( createGuestRequest.getAddress() );
        guest.setCheckInDate( createGuestRequest.getCheckInDate() );
        guest.setCheckOutDate( createGuestRequest.getCheckOutDate() );
        guest.setCountryCode( createGuestRequest.getCountryCode() );
        guest.setEmail( createGuestRequest.getEmail() );
        guest.setGuestUsageType( createGuestRequest.getGuestUsageType() );
        guest.setIdentityNumber( createGuestRequest.getIdentityNumber() );
        guest.setIsContact( createGuestRequest.getIsContact() );
        guest.setJob( createGuestRequest.getJob() );
        guest.setName( createGuestRequest.getName() );
        guest.setPhoneNumber( createGuestRequest.getPhoneNumber() );
        guest.setPlateNumber( createGuestRequest.getPlateNumber() );
        guest.setShortStay( createGuestRequest.getShortStay() );
        guest.setSurname( createGuestRequest.getSurname() );

        return guest;
    }

    @Override
    public void updateExistingGuestWithGuestRequest(UpdateGuestRequest updateGuestRequest, Guest existingGuest) {
        if ( updateGuestRequest == null ) {
            return;
        }

        existingGuest.setAddress( updateGuestRequest.getAddress() );
        if ( updateGuestRequest.getCheckInDate() != null ) {
            existingGuest.setCheckInDate( updateGuestRequest.getCheckInDate().atStartOfDay() );
        }
        else {
            existingGuest.setCheckInDate( null );
        }
        if ( updateGuestRequest.getCheckOutDate() != null ) {
            existingGuest.setCheckOutDate( updateGuestRequest.getCheckOutDate().atStartOfDay() );
        }
        else {
            existingGuest.setCheckOutDate( null );
        }
        existingGuest.setCountryCode( updateGuestRequest.getCountryCode() );
        existingGuest.setEmail( updateGuestRequest.getEmail() );
        existingGuest.setGuestUsageType( updateGuestRequest.getGuestUsageType() );
        existingGuest.setId( updateGuestRequest.getId() );
        existingGuest.setIdentityNumber( updateGuestRequest.getIdentityNumber() );
        existingGuest.setJob( updateGuestRequest.getJob() );
        existingGuest.setName( updateGuestRequest.getName() );
        existingGuest.setPhoneNumber( updateGuestRequest.getPhoneNumber() );
        existingGuest.setPlateNumber( updateGuestRequest.getPlateNumber() );
        existingGuest.setShortStay( updateGuestRequest.getShortStay() );
        existingGuest.setSurname( updateGuestRequest.getSurname() );
    }

    @Override
    public GuestDto toDto(Guest guest) {
        if ( guest == null ) {
            return null;
        }

        GuestDto guestDto = new GuestDto();

        guestDto.setAddress( guest.getAddress() );
        if ( guest.getCheckInDate() != null ) {
            guestDto.setCheckInDate( guest.getCheckInDate().toLocalDate() );
        }
        if ( guest.getCheckOutDate() != null ) {
            guestDto.setCheckOutDate( guest.getCheckOutDate().toLocalDate() );
        }
        guestDto.setCountryCode( guest.getCountryCode() );
        guestDto.setEmail( guest.getEmail() );
        guestDto.setGuestUsageType( guest.getGuestUsageType() );
        guestDto.setId( guest.getId() );
        guestDto.setIdentityNumber( guest.getIdentityNumber() );
        guestDto.setIsContact( guest.getIsContact() );
        guestDto.setJob( guest.getJob() );
        guestDto.setName( guest.getName() );
        guestDto.setPhoneNumber( guest.getPhoneNumber() );
        guestDto.setPlateNumber( guest.getPlateNumber() );
        guestDto.setShortStay( guest.getShortStay() );
        guestDto.setSurname( guest.getSurname() );

        return guestDto;
    }

    @Override
    public List<Guest> toEntityList(List<CreateGuestRequest> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Guest> list = new ArrayList<Guest>( dtoList.size() );
        for ( CreateGuestRequest createGuestRequest : dtoList ) {
            list.add( createGuestRequestToEntity( createGuestRequest ) );
        }

        return list;
    }
}
