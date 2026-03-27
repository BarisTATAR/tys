package com.tys.mapper;

import com.tys.dto.CafeDto;
import com.tys.dto.GuestDto;
import com.tys.model.Cafe;
import com.tys.model.Guest;
import com.tys.request.CreateCafeRequest;
import com.tys.request.UpdateCafeRequest;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CafeMapper {

    Cafe createCafeRequestToEntity(CreateCafeRequest createCafeRequest);

    void updateExistingCafeWithCafeRequest(UpdateCafeRequest updateCafeRequest, @MappingTarget Cafe existingCafe);
    CafeDto toDto(Cafe cafe);
}
