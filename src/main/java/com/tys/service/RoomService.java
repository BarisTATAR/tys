package com.tys.service;

import com.tys.model.Room;
import com.tys.repository.CompanyRepository;
import com.tys.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room saveRoom(Room room){

        return roomRepository.save(room);

    }

   public void deleteRoom(Long roomNumber){

         roomRepository.deleteById(roomNumber);
   }


}
