package com.tys.service;

import com.tys.model.Guest;
import com.tys.repository.GuestRepository;
import com.tys.request.CreateGuestRequest;
import com.tys.request.DeleteGuestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    public void createGuest(CreateGuestRequest request) {
        //Guest guest = modelMapper.map(request, Guest.class);
        //guestRepository.save(guest);
    }

    public void deleteGuest(DeleteGuestRequest request) {
        if (!guestRepository.existsById(request.getId())) {
            throw new RuntimeException("Guests not found with Id: " + request.getId());
        }
        guestRepository.deleteById(request.getId());
    }
}