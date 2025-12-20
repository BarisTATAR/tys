package com.tys.mapper;

import com.tys.dto.RoomDto;
import com.tys.model.Room;
import com.tys.request.CreateRoomRequest;
import com.tys.request.UpdateRoomRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomMapper {
    Room createRoomRequestToEntity(CreateRoomRequest createRoomRequest);

    //@Mapping(source = "id", target = "id", ignore = true)
    void updateExistingRoomWithRoomRequest(UpdateRoomRequest updateRoomRequest, @MappingTarget Room existingRoom);
    RoomDto toDto(Room room);
    List<RoomDto> toDtoList(List<Room> rooms);
}
