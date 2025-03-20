package com.tys.controller;


import com.tys.model.Guest;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.CreateGuestRequest;
import com.tys.request.DeleteCompanyRequest;
import com.tys.request.DeleteGuestRequest;
import com.tys.service.GuestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/guests")
public class GuestController {

    private GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping
    public ResponseEntity<Void> saveGuest(@RequestBody CreateGuestRequest request) {
        guestService.saveGuest(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{request}")
    public ResponseEntity<Void> deleteGuest(@ModelAttribute DeleteGuestRequest request) {
        guestService.deleteGuest(request);
        return ResponseEntity.ok().build();
    }

}
