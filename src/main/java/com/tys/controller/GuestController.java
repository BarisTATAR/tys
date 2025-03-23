package com.tys.controller;


import com.tys.model.Guest;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.CreateGuestRequest;
import com.tys.request.DeleteCompanyRequest;
import com.tys.request.DeleteGuestRequest;
import com.tys.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guests")
public class GuestController {

    private final GuestService guestService;

    @PostMapping
    public ResponseEntity<Void> createGuest(@RequestBody CreateGuestRequest request) {
        guestService.createGuest(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteGuest(@RequestBody DeleteGuestRequest request) {
        guestService.deleteGuest(request);
        return ResponseEntity.ok().build();
    }

}
