package com.tys.controller;

import com.tys.dto.RoomDto;
import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteRoomRequest;
import com.tys.request.UpdateRoomRequest;
import com.tys.security.CurrentActor;
import com.tys.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;
    private final CurrentActor currentActor;

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest request) {
        try {
            boolean admin = currentActor.isAdmin();
            Long companyId = admin ? null : currentActor.requireCompanyId();
            roomService.createRoom(request, companyId, admin);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException ex) {
            throw ex;
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
        boolean admin = currentActor.isAdmin();
        Long companyId = admin ? null : currentActor.requireCompanyId();
        roomService.deleteRoom(request, companyId, admin);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateRoom(@RequestBody UpdateRoomRequest request) {
        boolean admin = currentActor.isAdmin();
        Long companyId = admin ? null : currentActor.requireCompanyId();
        roomService.updateRoom(request, companyId, admin);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/alls")
    public ResponseEntity<List<RoomDto>> getAllRooms() {
        if (currentActor.isAdmin()) {
            return ResponseEntity.ok(roomService.getAllRooms());
        }
        return ResponseEntity.ok(roomService.getAllRoomsByCompanyId(currentActor.requireCompanyId()));
    }

    @GetMapping("/company-rooms")
    public ResponseEntity<?> getAllRoomsByCompanyId() {
        try {
            Long companyId = getCurrentCompanyIdFromAuth();
            List<RoomDto> rooms = roomService.getAllRoomsByCompanyId(companyId);
            return ResponseEntity.ok(rooms);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Room bilgileri alınamadı: " + e.getMessage());
            error.put("error", "ROOM_FETCH_ERROR");
            return ResponseEntity.status(500).body(error);
        }
    }

    private Long getCurrentCompanyIdFromAuth() {
        return currentActor.requireCompanyId();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id) {
        boolean admin = currentActor.isAdmin();
        Long companyId = admin ? null : currentActor.requireCompanyId();
        return ResponseEntity.ok(roomService.getRoomById(id, companyId, admin));
    }

    // Company ID ile odaları getir (COMPANY yalnızca kendi işletmesi; ADMIN tümü)
    @GetMapping("/by-company/{companyId}")
    public ResponseEntity<?> getRoomsByCompanyId(@PathVariable Long companyId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (!isAdmin) {
                Long currentCompanyId = getCurrentCompanyIdFromAuth();
                if (!currentCompanyId.equals(companyId)) {
                    Map<String, String> forbidden = new HashMap<>();
                    forbidden.put("message", "Bu işlem için yetkiniz yok.");
                    forbidden.put("error", "FORBIDDEN");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(forbidden);
                }
            }
            List<RoomDto> rooms = roomService.getAllRoomsByCompanyId(companyId);
            return ResponseEntity.ok(rooms);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Room bilgileri alınamadı: " + e.getMessage());
            error.put("error", "ROOM_FETCH_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Boş odaları al (excludeReservationId: güncellemede mevcut rezervasyonun odaları da listede kalsın)
    @GetMapping("/availableRooms")
    public List<RoomDto> getAvailableRooms(
            @RequestParam("checkInDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInDate,
            @RequestParam("checkOutDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutDate,
            @RequestParam(value = "excludeReservationId", required = false) Long excludeReservationId) {
        Long companyId = getCurrentCompanyIdFromAuth();
        return roomService.getAvailableRoomsExcludingReservation(checkInDate, checkOutDate, companyId, excludeReservationId);
    }
}
