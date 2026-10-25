package com.tys.mapper;

import com.tys.dto.RoomDto;
import com.tys.model.Room;
import com.tys.request.CreateRoomRequest;
import com.tys.request.UpdateRoomRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoomMapper {
    Room createRoomRequestToEntity(CreateRoomRequest createRoomRequest);

    @Mapping(target = "id", ignore = true)
    void updateExistingRoomWithRoomRequest(UpdateRoomRequest updateRoomRequest, @MappingTarget Room existingRoom);

    /**
     * Ana ekran oda tahtası için rezervasyonlar dahil maplenir.
     * Rezervasyon DTO içindeki roomList için {@link ReservationMapper#roomsToSimpleRoomList} kullanılır
     * (circular mapping olmaması için).
     */
    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "company.name", target = "companyName")
    RoomDto toDto(Room room);

    List<RoomDto> toDtoList(List<Room> rooms);
}
