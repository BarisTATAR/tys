package com.tys.service;

import com.tys.model.Room;
import com.tys.repository.RoomRepository;
import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteRoomRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public void createRoom(CreateRoomRequest request) {
        //Room room = modelMapper.map(request, Room.class);
        //roomRepository.save(room);
    }

    public void deleteRoom(DeleteRoomRequest request) {
        if (!roomRepository.existsById(request.getId())) {
            throw new RuntimeException("Room not found with Id: " + request.getId());
        }
        roomRepository.deleteById(request.getId());
    }
}



