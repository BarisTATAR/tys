package com.tys.controller;

import com.tys.dto.GuestDto;
import com.tys.model.Guest;
import com.tys.request.CreateGuestRequest;
import com.tys.request.DeleteGuestRequest;
import com.tys.request.UpdateGuestRequest;
import com.tys.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guest")
@CrossOrigin(origins = "http://localhost:3000")
public class GuestController {

    private final GuestService guestService;

    @PostMapping("/create")
    public ResponseEntity<Void> createGuest(@RequestBody CreateGuestRequest request) {
        guestService.createGuest(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteGuest(@RequestBody DeleteGuestRequest request) {
        guestService.deleteGuest(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateGuest(@RequestBody UpdateGuestRequest request) {
        guestService.updateGuest(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{identityNumber}")
    public ResponseEntity<GuestDto> getGuestByIdentityNumber(@PathVariable("identityNumber") String identityNumber) {
        return ResponseEntity.ok(guestService.getGuestByIdentityNumber(identityNumber));
    }

    @GetMapping("/all")
    public ResponseEntity<List<GuestDto>> getAllGuest() {
        return ResponseEntity.ok(guestService.getAllGuest());
    }
}