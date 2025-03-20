package com.tys.controller;

import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteRoomRequest;
import com.tys.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }


    @PostMapping
    public ResponseEntity<Void> saveRoom(@RequestBody CreateRoomRequest request) {
        roomService.saveRoom(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{request}")
    public ResponseEntity<Void> deleteRoom(@ModelAttribute DeleteRoomRequest request) {
        roomService.deleteRoom(request);
        return ResponseEntity.ok().build();
    }


}
