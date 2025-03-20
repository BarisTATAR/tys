package com.tys.service;

import com.tys.model.Room;
import com.tys.repository.RoomRepository;
import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteCompanyRequest;
import com.tys.request.DeleteRoomRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;
    private ModelMapper modelMapper;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
        this.modelMapper = new ModelMapper();
    }

    public void saveRoom(CreateRoomRequest request) {
        Room room = modelMapper.map(request, Room.class);
        roomRepository.save(room);


    }

    public void deleteRoom(DeleteRoomRequest request) {

        if (!roomRepository.existsById(request.getId())) {

            throw new RuntimeException("Room not found with Id: " + request.getId());

        }
        roomRepository.deleteById(request.getId());
    }
}



