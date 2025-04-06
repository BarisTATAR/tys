package com.tys.mapper;

import com.tys.model.Room;
import com.tys.request.CreateRoomRequest;
import com.tys.request.UpdateRoomRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    Room createRoomRequestToEntity(CreateRoomRequest createRoomRequest);

    @Mapping(source = "id", target = "id", ignore = true)
    void updateExistingRoomWithRoomRequest(UpdateRoomRequest updateRoomRequest, @MappingTarget Room existingRoom);
}
