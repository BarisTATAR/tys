package com.tys.controller;

import com.tys.dto.RoomDto;
import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteRoomRequest;
import com.tys.request.UpdateRoomRequest;
import com.tys.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
@CrossOrigin(origins = "http://localhost:3000")
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest request) {
        try {
            roomService.createRoom(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            // Business rule hatasını yakala ve mesaj olarak döndür
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            error.put("error", "ROOM_CREATE_ERROR");
            return ResponseEntity
                    .badRequest() // 400 Bad Request
                    .body(error);
        }
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

    @GetMapping("/alls")
    public ResponseEntity<List<RoomDto>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/company-rooms")
    public ResponseEntity<?> getAllRoomsByCompanyId() {
        try {
            Long companyId = getCurrentCompanyIdFromAuth();
            List<RoomDto> rooms = roomService.getAllRoomsByCompanyId(companyId);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Room bilgileri alınamadı: " + e.getMessage());
            error.put("error", "ROOM_FETCH_ERROR");
            return ResponseEntity.status(500).body(error);
        }
    }

    private Long getCurrentCompanyIdFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new IllegalStateException("Kimlik doğrulanamadı.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    // Company ID ile odaları getir
    @GetMapping("/by-company/{companyId}")
    public ResponseEntity<?> getRoomsByCompanyId(@PathVariable Long companyId) {
        try {
            List<RoomDto> rooms = roomService.getAllRoomsByCompanyId(companyId);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Room bilgileri alınamadı: " + e.getMessage());
            error.put("error", "ROOM_FETCH_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Boş odaları al
    @GetMapping("/availableRooms")
    public List<RoomDto> getAvailableRooms(
            @RequestParam("checkInDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInDate,
            @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutDate) {
        return roomService.getAvailableRooms(checkInDate, checkOutDate);
    }
}
