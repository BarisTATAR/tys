package com.tys.service;

import com.tys.dto.GuestDto;
import com.tys.mapper.GuestMapper;
import com.tys.model.Company;
import com.tys.model.Guest;
import com.tys.model.Reservation;
import com.tys.repository.CompanyRepository;
import com.tys.repository.GuestRepository;
import com.tys.request.CreateGuestRequest;
import com.tys.validation.InputRules;
import com.tys.request.DeleteGuestRequest;
import com.tys.request.UpdateGuestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final CompanyRepository companyRepository;
    private final GuestMapper guestMapper;

    public void createGuest(CreateGuestRequest request, Long companyId) {
        InputRules.guestFields(
                request.getName(),
                request.getSurname(),
                request.getIdentityNumber(),
                request.getPhoneNumber(),
                request.getEmail(),
                request.getPlateNumber(),
                null,
                null);
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Misafir adı boş olamaz.");
        }
        if (request.getIdentityNumber() == null || request.getIdentityNumber().isBlank()) {
            throw new IllegalArgumentException("TCKN gereklidir.");
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
        Guest guest = guestMapper.createGuestRequestToEntity(request);
        guest.setCompany(company);
        guestRepository.save(guest);
    }

    @Transactional
    public void deleteGuest(DeleteGuestRequest request, Long companyId) {
        Guest guest = guestRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Guests not found with Id: " + request.getId()));
        assertGuestCompany(guest, companyId);
        guestRepository.delete(guest);
    }

    @Transactional
    public void updateGuest(UpdateGuestRequest request, Long companyId) {
        Guest existingGuest = guestRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Guest not found with Id: " + request.getId()));
        assertGuestCompany(existingGuest, companyId);
        InputRules.guestFields(
                request.getName(),
                request.getSurname(),
                request.getIdentityNumber(),
                request.getPhoneNumber(),
                request.getEmail(),
                request.getPlateNumber(),
                existingGuest.getIdentityNumber(),
                existingGuest.getPhoneNumber());
        guestMapper.updateExistingGuestWithGuestRequest(request, existingGuest);
        if (request.getCheckInDate() != null) {
            existingGuest.setCheckInDate(request.getCheckInDate().atStartOfDay());
        }
        if (request.getCheckOutDate() != null) {
            existingGuest.setCheckOutDate(request.getCheckOutDate().atStartOfDay());
        }
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

    @Transactional(readOnly = true)
    public GuestDto getGuestByIdentityNumber(String identityNumber, Long companyId) {
        Guest guest = guestRepository.findByIdentityNumber(identityNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Misafir bulunamadı."));
        assertGuestCompany(guest, companyId);
        return guestMapper.toDto(guest);
    }

    public void assertPhonesBelongToCompany(List<String> phones, Long companyId) {
        if (phones == null || phones.isEmpty()) {
            throw new RuntimeException("SMS için numara gerekli.");
        }
        Set<String> allowed = new HashSet<>();
        for (String phone : getAllGuestForSMS(companyId)) {
            allowed.add(phoneKey(phone));
        }
        for (String phone : phones) {
            if (!allowed.contains(phoneKey(phone))) {
                throw new RuntimeException("SMS yalnızca bu işletmenin kayıtlı misafir numaralarına gönderilebilir.");
            }
        }
    }

    private void assertGuestCompany(Guest guest, Long companyId) {
        if (guest.getCompany() != null && Objects.equals(guest.getCompany().getId(), companyId)) {
            return;
        }
        if (guest.getReservations() != null) {
            for (Reservation reservation : guest.getReservations()) {
                if (reservation.getCompany() != null && Objects.equals(reservation.getCompany().getId(), companyId)) {
                    return;
                }
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
    }

    private String phoneKey(String phone) {
        if (phone == null) {
            return "";
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() > 10) {
            digits = digits.substring(digits.length() - 10);
        }
        return digits;
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
        return guestRepository.findAllByCompanyIdWithReservations(companyId)
                .stream()
                .filter(guest -> matchesStayDayCriteria(guest, companyId, day))
                .map(Guest::getPhoneNumber)
                .filter(phone -> phone != null && !phone.isBlank())
                .distinct()
                .toList();
    }

    private boolean matchesStayDayCriteria(Guest guest, Long companyId, int day) {
        if (guest.getCheckInDate() != null && guest.getCheckOutDate() != null) {
            long stayDays = calculateStayDays(guest.getCheckInDate(), guest.getCheckOutDate());
            if (matchesStayDays(stayDays, day)) {
                return true;
            }
        }
        if (guest.getReservations() == null) {
            return false;
        }
        return guest.getReservations().stream()
                .filter(reservation -> reservation.getCompany() != null
                        && Objects.equals(reservation.getCompany().getId(), companyId))
                .filter(reservation -> reservation.getCheckInDate() != null && reservation.getCheckOutDate() != null)
                .map(reservation -> calculateStayDays(reservation.getCheckInDate(), reservation.getCheckOutDate()))
                .anyMatch(stayDays -> matchesStayDays(stayDays, day));
    }

    private static long calculateStayDays(LocalDateTime checkIn, LocalDateTime checkOut) {
        return ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
    }

    private static boolean matchesStayDays(long stayDays, int day) {
        return day >= 5 ? stayDays >= day : stayDays == day;
    }
}