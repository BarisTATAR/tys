package com.tys.mapper;

import com.tys.dto.GuestDto;
import com.tys.model.Guest;
import com.tys.request.CreateGuestRequest;
import com.tys.request.UpdateGuestRequest;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GuestMapper {
    Guest createGuestRequestToEntity(CreateGuestRequest createGuestRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "checkInDate", ignore = true)
    @Mapping(target = "checkOutDate", ignore = true)
    void updateExistingGuestWithGuestRequest(UpdateGuestRequest updateGuestRequest, @MappingTarget Guest existingGuest);

    GuestDto toDto(Guest guest);

    List<Guest> toEntityList(List<CreateGuestRequest> dtoList);

}