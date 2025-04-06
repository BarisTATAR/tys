package com.tys.controller;

import com.tys.model.Room;
import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteRoomRequest;
import com.tys.request.UpdateRoomRequest;
import com.tys.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/create")
    public ResponseEntity<Void> createRoom(@RequestBody CreateRoomRequest request) {
        roomService.createRoom(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteRoom(@RequestBody DeleteRoomRequest request) {
        roomService.deleteRoom(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateRoom(@RequestBody UpdateRoomRequest request) {
        roomService.updateRoom(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/alls")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }
}
