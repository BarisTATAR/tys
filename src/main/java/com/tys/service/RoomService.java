package com.tys.service;

import com.tys.mapper.RoomMapper;
import com.tys.model.Company;
import com.tys.model.Room;
import com.tys.repository.CompanyRepository;
import com.tys.repository.RoomRepository;
import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteRoomRequest;
import com.tys.request.UpdateRoomRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final CompanyRepository companyRepository;
    private final RoomMapper roomMapper;

    public void createRoom(CreateRoomRequest request) {
        Room room = roomMapper.createRoomRequestToEntity(request);
        Optional<Company> company = companyRepository.findById(request.getCompanyId());
        if (company.isEmpty()) {
            throw new RuntimeException("Company not found with Id: " + request.getCompanyId());
        } else {
            room.setCompany(company.get());
        }
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

    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found with Id: " + id));
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
}



