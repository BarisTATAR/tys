package com.tys.controller;


import com.tys.model.Guest;
import com.tys.service.GuestService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/guests")
public class GuestController {

    private GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping
    public Guest saveGuest(@RequestBody Guest guest){

        return guestService.saveGuest(guest);
    }
    @DeleteMapping("/{id}")
    public void deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);

    }

}
