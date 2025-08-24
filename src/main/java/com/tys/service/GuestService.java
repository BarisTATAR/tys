package com.tys.service;

import com.tys.dto.GuestDto;
import com.tys.mapper.GuestMapper;
import com.tys.model.Guest;
import com.tys.repository.GuestRepository;
import com.tys.request.CreateGuestRequest;
import com.tys.request.DeleteGuestRequest;
import com.tys.request.UpdateGuestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<GuestDto> getAllGuest() {
        return guestRepository.findAll()
                .stream()
                .map(guestMapper::toDto)
                .toList();    }

    public GuestDto getGuestByIdentityNumber(String identityNumber) {
        Guest guest = guestRepository.findByIdentityNumber(identityNumber)
                .orElseThrow(() -> new RuntimeException("Guest not found with IdentityNumber: " + identityNumber));
        return guestMapper.toDto(guest);
    }
}