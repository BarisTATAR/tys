package com.tys.service;

import com.tys.dto.GuestDto;
import com.tys.mapper.GuestMapper;
import com.tys.model.Company;
import com.tys.model.Guest;
import com.tys.repository.CompanyRepository;
import com.tys.repository.GuestRepository;
import com.tys.request.CreateGuestRequest;
import com.tys.request.DeleteGuestRequest;
import com.tys.request.UpdateGuestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final CompanyRepository companyRepository;
    private final GuestMapper guestMapper;

    public void createGuest(CreateGuestRequest request, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
        Guest guest = guestMapper.createGuestRequestToEntity(request);
        guest.setCompany(company);
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

    public List<GuestDto> getAllGuest(Long companyId) {
        return guestRepository.findAllByCompanyId(companyId)
                .stream()
                .map(guestMapper::toDto)
                .toList();
    }

    public List<String> getAllGuestForSMS(Long companyId) {
        return guestRepository.findAllByCompanyId(companyId)
                .stream()
                .map(Guest::getPhoneNumber)
                .filter(phone -> phone != null && !phone.isBlank())
                .distinct()
                .toList();
    }

    public GuestDto getGuestByIdentityNumber(String identityNumber) {
        Guest guest = guestRepository.findByIdentityNumber(identityNumber)
                .orElseThrow(() -> new RuntimeException("Guest not found with IdentityNumber: " + identityNumber));
        return guestMapper.toDto(guest);
    }

    public List<String> getAllGuestInYear(int year, Long companyId) {
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        return guestRepository.findAllByCheckInYearAndCompanyId(start, end, companyId)
                .stream()
                .map(Guest::getPhoneNumber)
                .filter(phone -> phone != null && !phone.isBlank())
                .distinct()
                .toList();
    }

    public List<String> getAllGuestForDay(int day, Long companyId) {
        List<Guest> companyGuests = guestRepository.findAllByCompanyId(companyId);
        if (day >= 5) {
            return companyGuests.stream()
                    .filter(g -> g.getCheckInDate() != null && g.getCheckOutDate() != null)
                    .filter(g -> {
                        long stayDays = ChronoUnit.DAYS.between(
                                g.getCheckInDate().toLocalDate(),
                                g.getCheckOutDate().toLocalDate()
                        );
                        return stayDays >= day;
                    })
                    .map(Guest::getPhoneNumber)
                    .filter(phone -> phone != null && !phone.isBlank())
                    .distinct()
                    .toList();
        } else {
            return companyGuests.stream()
                    .filter(g -> g.getCheckInDate() != null && g.getCheckOutDate() != null)
                    .filter(g -> {
                        long stayDays = ChronoUnit.DAYS.between(
                                g.getCheckInDate().toLocalDate(),
                                g.getCheckOutDate().toLocalDate()
                        );
                        return stayDays == day;
                    })
                    .map(Guest::getPhoneNumber)
                    .filter(phone -> phone != null && !phone.isBlank())
                    .distinct()
                    .toList();
        }
    }
}