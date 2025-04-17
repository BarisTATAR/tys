package com.tys.mapper;

import com.tys.model.Rating;
import com.tys.request.CreateRatingRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatingMapper {

    @Mapping(source = "id", target = "id", ignore = true)
    Rating createRatingRequestToEntity(CreateRatingRequest createRatingRequest);
}
