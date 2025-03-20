package com.tys.service;

import com.tys.model.Guest;
import com.tys.repository.GuestRepository;
import com.tys.request.CreateGuestRequest;
import com.tys.request.DeleteGuestRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuestService {

    @Autowired
    private GuestRepository guestRepository;
    private ModelMapper modelMapper;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
        this.modelMapper = new ModelMapper();
    }

    public void saveGuest(CreateGuestRequest request) {
        Guest guest = modelMapper.map(request, Guest.class);
        guestRepository.save(guest);

    }

    public void deleteGuest(DeleteGuestRequest request) {
        if (!guestRepository.existsById(request.getId())) {

            throw new RuntimeException("Guests not found with Id: " + request.getId());

        }
        guestRepository.deleteById(request.getId());

    }

}