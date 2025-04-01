package com.tys.mapper;


import com.tys.model.Reservation;
import com.tys.request.CreateReservationRequest;
import com.tys.request.UpdateReservationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface ReservationMapper {
    Reservation createReservationRequestToEntity(CreateReservationRequest createReservationRequest);

    @Mapping(source ="id", target = "id", ignore = true)
    void updateExistingReservationWithReservationRequest(UpdateReservationRequest updateReservationRequest, @MappingTarget Reservation existingrReservation);
}
