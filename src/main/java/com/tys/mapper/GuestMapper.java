package com.tys.mapper;

import com.tys.dto.GuestDto;
import com.tys.model.Guest;
import com.tys.request.CreateGuestRequest;
import com.tys.request.UpdateGuestRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GuestMapper {
    Guest createGuestRequestToEntity(CreateGuestRequest createGuestRequest);

    void updateExistingGuestWithGuestRequest(UpdateGuestRequest updateGuestRequest, @MappingTarget Guest existingGuest);

    GuestDto toDto(Guest guest);

    List<Guest> toEntityList(List<CreateGuestRequest> dtoList);

}