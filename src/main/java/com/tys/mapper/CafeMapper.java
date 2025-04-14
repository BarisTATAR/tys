package com.tys.mapper;

import com.tys.model.Cafe;
import com.tys.request.CreateCafeRequest;
import com.tys.request.UpdateCafeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CafeMapper {

    Cafe createCafeRequestToEntity(CreateCafeRequest createCafeRequest);

    @Mapping(source = "id", target = "id", ignore = true)
    void updateExistingCafeWithCafeRequest(UpdateCafeRequest updateCafeRequest, @MappingTarget Cafe existingCafe);

}
