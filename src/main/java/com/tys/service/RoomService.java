package com.tys.service;

import com.tys.dto.RoomDto;
import com.tys.enums.ReservationStatus;
import com.tys.mapper.RoomMapper;
import com.tys.model.Company;
import com.tys.model.Reservation;
import com.tys.model.Room;
import com.tys.repository.CompanyRepository;
import com.tys.repository.ReservationRepository;
import com.tys.repository.RoomRepository;
import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteRoomRequest;
import com.tys.request.UpdateRoomRequest;
import com.tys.util.ReservationDateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final CompanyRepository companyRepository;
    private final RoomMapper roomMapper;

    public void createRoom(CreateRoomRequest request, Long actorCompanyId, boolean admin) {
        Company company = resolveCompany(request, actorCompanyId, admin);
        Room room = roomMapper.createRoomRequestToEntity(request);
        long currentRoomCount = roomRepository.countByCompany(company);

        if (currentRoomCount >= company.getTotalRoomNumber()) {
            throw new RuntimeException("Toplam oda sayısı sınırı aşıldı. Maksimum izin verilen oda sayısı: " + company.getTotalRoomNumber());
        }
        room.setCompany(company);
        roomRepository.save(room);
    }

    public void deleteRoom(DeleteRoomRequest request, Long actorCompanyId, boolean admin) {
        Room room = roomRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oda bulunamadı."));
        assertRoomCompany(room, actorCompanyId, admin);
        roomRepository.delete(room);
    }

    public void updateRoom(UpdateRoomRequest request, Long actorCompanyId, boolean admin) {
        Room existingRoom = roomRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oda bulunamadı."));
        assertRoomCompany(existingRoom, actorCompanyId, admin);
        Company owner = existingRoom.getCompany();
        roomMapper.updateExistingRoomWithRoomRequest(request, existingRoom);
        existingRoom.setCompany(owner);
        roomRepository.save(existingRoom);
    }

    public RoomDto getRoomById(Long id, Long actorCompanyId, boolean admin) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oda bulunamadı."));
        assertRoomCompany(room, actorCompanyId, admin);
        return roomMapper.toDto(room);
    }

    private Company resolveCompany(CreateRoomRequest request, Long actorCompanyId, boolean admin) {
        if (!admin) {
            return companyRepository.findById(actorCompanyId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İşletme bulunamadı."));
        }
        if (request.getCompanyId() != null) {
            return companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İşletme bulunamadı."));
        }
        if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "İşletme seçilmelidir.");
        }
        return companyRepository.findByName(request.getCompanyName().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İşletme bulunamadı."));
    }

    private void assertRoomCompany(Room room, Long actorCompanyId, boolean admin) {
        if (admin) {
            return;
        }
        if (actorCompanyId == null || room.getCompany() == null || !actorCompanyId.equals(room.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
        }
    }

    public List<RoomDto> getAllRooms() {
        return roomRepository.findAllWithReservations().stream().map(roomMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRooms(LocalDateTime checkInDate, LocalDateTime checkOutDate, Long companyId) {
        return getAvailableRoomsExcludingReservation(checkInDate, checkOutDate, companyId, null);
    }

    /**
     * Seçilen tarihlerde müsait odalar.
     * 1) Native SQL ile dolu oda id'leri
     * 2) Yedek: entity üzerinden gün bazlı çakışma
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRoomsExcludingReservation(
            LocalDateTime checkInDate,
            LocalDateTime checkOutDate,
            Long companyId,
            Long excludeReservationId) {

        LocalDate reqIn = ReservationDateUtils.toLocalDate(checkInDate);
        LocalDate reqOut = ReservationDateUtils.toLocalDate(checkOutDate);
        // Aynı gün giriş-çıkışa izin ver; müsaitlik için aynı gün [in, in+1) aralığı gibi işlenir
        if (reqIn == null || reqOut == null || reqOut.isBefore(reqIn)) {
            return List.of();
        }
        LocalDate queryOut = reqOut.isEqual(reqIn) ? reqIn.plusDays(1) : reqOut;

        Set<Long> occupiedRoomIds = new HashSet<>();
        try {
            List<Long> nativeOccupied = reservationRepository.findOccupiedRoomIdsNative(
                    companyId, reqIn, queryOut, excludeReservationId);
            if (nativeOccupied != null) {
                occupiedRoomIds.addAll(nativeOccupied);
            }
        } catch (Exception ignored) {
            // Native sorgu DB/enum farkında patlarsa entity yedek yolu kullanılır
        }

        // Yedek / birleştir: owning side rezervasyonlardan da doldur
        occupiedRoomIds.addAll(findOccupiedRoomIdsFromEntities(companyId, reqIn, queryOut, excludeReservationId));

        return roomRepository.findAllByCompanyIdWithReservations(companyId).stream()
                .filter(room -> room.getId() != null && !occupiedRoomIds.contains(room.getId()))
                .sorted(Comparator.comparing(Room::getNumber, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toAvailableRoomDto)
                .toList();
    }

    private Set<Long> findOccupiedRoomIdsFromEntities(
            Long companyId,
            LocalDate reqIn,
            LocalDate reqOut,
            Long excludeReservationId) {

        Set<Long> occupied = new HashSet<>();
        List<Long> reservationIds = reservationRepository.findReservationIdsByCompanyId(companyId);
        if (reservationIds == null || reservationIds.isEmpty()) {
            return occupied;
        }

        List<Reservation> reservations = reservationRepository.findWithRoomsByReservationIds(reservationIds);
        for (Reservation res : reservations) {
            if (res == null) {
                continue;
            }
            if (excludeReservationId != null && excludeReservationId.equals(res.getId())) {
                continue;
            }
            if (res.getReservationStatus() == ReservationStatus.CHECK_OUT_DONE) {
                continue;
            }
            LocalDate resIn = ReservationDateUtils.toLocalDate(res.getCheckInDate());
            LocalDate resOut = ReservationDateUtils.toLocalDate(res.getCheckOutDate());
            if (resIn == null || resOut == null) {
                continue;
            }
            // Aynı gün rezervasyonlar dolulukta [in, in+1) gibi sayılır
            LocalDate effectiveResOut = resOut.isEqual(resIn) ? resIn.plusDays(1) : resOut;
            if (resIn.isBefore(reqOut) && effectiveResOut.isAfter(reqIn)) {
                if (res.getRooms() == null) {
                    continue;
                }
                for (Room room : res.getRooms()) {
                    if (room != null && room.getId() != null) {
                        occupied.add(room.getId());
                    }
                }
            }
        }
        return occupied;
    }

    public boolean isRoomAvailableForDates(Room room, LocalDate reqIn, LocalDate reqOut, Long excludeReservationId) {
        if (room == null || room.getId() == null || room.getCompany() == null) {
            return false;
        }
        if (reqIn == null || reqOut == null || reqOut.isBefore(reqIn)) {
            return false;
        }
        LocalDate queryOut = reqOut.isEqual(reqIn) ? reqIn.plusDays(1) : reqOut;
        Set<Long> occupied = new HashSet<>();
        try {
            occupied.addAll(reservationRepository.findOccupiedRoomIdsNative(
                    room.getCompany().getId(), reqIn, queryOut, excludeReservationId));
        } catch (Exception ignored) {
            // ignore
        }
        occupied.addAll(findOccupiedRoomIdsFromEntities(
                room.getCompany().getId(), reqIn, queryOut, excludeReservationId));
        return !occupied.contains(room.getId());
    }

    private RoomDto toAvailableRoomDto(Room room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setNumber(room.getNumber());
        dto.setCapacity(room.getCapacity());
        dto.setFloor(room.getFloor());
        dto.setSeaView(room.getSeaView());
        dto.setLoaded(room.getLoaded());
        if (room.getCompany() != null) {
            dto.setCompanyId(room.getCompany().getId());
            dto.setCompanyName(room.getCompany().getName());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<RoomDto> getAllRoomsByCompanyId(Long companyId) {
        return roomRepository.findAllByCompanyIdWithReservations(companyId).stream().map(roomMapper::toDto).toList();
    }
}
