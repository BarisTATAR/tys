package com.tys.service;

import com.tys.model.Guest;
import com.tys.repository.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuestService {

    @Autowired
    private GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public Guest saveGuest(Guest guest) {
        return guestRepository.save(guest);

    }

    public Guest deleteGuest(Long guestId) {
        guestRepository.deleteById(guestId);

        return null;
    }
}