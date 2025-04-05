package com.tys.mapper;

import com.tys.model.Guest;
import com.tys.request.CreateGuestRequest;
import com.tys.request.UpdateGuestRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GuestMapper {

    Guest createGuestRequestToEntity(CreateGuestRequest createGuestRequest);

    @Mapping(source = "id", target = "id", ignore = true)
    void updateExistingGuestWithGuestRequest(UpdateGuestRequest updateGuestRequest, @MappingTarget Guest existingGuest);

}