package com.tys.service;

import com.tys.mapper.GuestMapper;
import com.tys.model.Company;
import com.tys.model.Guest;
import com.tys.repository.GuestRepository;
import com.tys.request.CreateGuestRequest;
import com.tys.request.DeleteGuestRequest;
import com.tys.request.UpdateCompanyRequest;
import com.tys.request.UpdateGuestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final GuestMapper guestMapper;

    public void createGuest(CreateGuestRequest request) {
        Guest guest = guestMapper.createGuestRequestToEntity(request);
        guestRepository.save(guest);
    }

    public void deleteGuest(DeleteGuestRequest request) {
        if (!guestRepository.existsById(request.getId())) {
            throw new RuntimeException("Guests not found with Id: " + request.getId());
        }
        guestRepository.deleteById(request.getId());
    }

    public void updateGuest(UpdateGuestRequest request) {
        Guest existingGuest = guestRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Guest not found with Id: " + request.getId()));
        guestMapper.updateExistingGuestWithGuestRequest(request, existingGuest);
        guestRepository.save(existingGuest);
    }
}