package com.tys.controller;

import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteRoomRequest;
import com.tys.request.UpdateGuestRequest;
import com.tys.request.UpdateRoomRequest;
import com.tys.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<Void> createRoom(@RequestBody CreateRoomRequest request) {
        roomService.createRoom(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteRoom(@RequestBody DeleteRoomRequest request) {
        roomService.deleteRoom(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateRoom(@RequestBody UpdateRoomRequest request) {
        roomService.updateRoom(request);
        return ResponseEntity.ok().build();
    }

}
