package com.tys.service;

import com.tys.mapper.GuestMapper;
import com.tys.mapper.RoomMapper;
import com.tys.model.Guest;
import com.tys.model.Room;
import com.tys.repository.RoomRepository;
import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteRoomRequest;
import com.tys.request.UpdateRoomRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public void createRoom(CreateRoomRequest request) {
        Room room = roomMapper.createRoomRequestToEntity(request);
        roomRepository.save(room);
    }

    public void deleteRoom(DeleteRoomRequest request) {
        if (!roomRepository.existsById(request.getId())) {
            throw new RuntimeException("Room not found with Id: " + request.getId());
        }
        roomRepository.deleteById(request.getId());
    }

    public void updateRoom(UpdateRoomRequest request) {
        Room existingRoom = roomRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Room not found with Id: " + request.getId()));
        roomMapper.updateExistingRoomWithRoomRequest(request, existingRoom);
        roomRepository.save(existingRoom);

    }
}



