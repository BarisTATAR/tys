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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final CompanyRepository companyRepository;
    private final RoomMapper roomMapper;

    public void createRoom(CreateRoomRequest request) {
        Room room = roomMapper.createRoomRequestToEntity(request);
        Optional<Company> company = companyRepository.findByName(request.getCompanyName());
        if (company.isEmpty()) {
            throw new RuntimeException("Company not found with Id: " + request.getCompanyId());
        }
        long currentRoomCount = roomRepository.countByCompany(company.get());

        if (currentRoomCount >= company.get().getTotalRoomNumber()) {
            throw new RuntimeException("Toplam oda sayısı sınırı aşıldı. Maksimum izin verilen oda sayısı: " + company.get().getTotalRoomNumber());
        }
        room.setCompany(company.get());
        roomRepository.save(room);
    }

    public void deleteRoom(DeleteRoomRequest request) {
        if (!roomRepository.existsById(request.getId())) {
            throw new RuntimeException("Room not found with Id: " + request.getId());
        }
        roomRepository.deleteById(request.getId());
    }

    public void updateRoom(UpdateRoomRequest request) {
        Room existingRoom = roomRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Room not found with Id: " + request.getId()));
        roomMapper.updateExistingRoomWithRoomRequest(request, existingRoom);
        roomRepository.save(existingRoom);
    }

    public RoomDto getRoomById(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found with Id: " + id));
        return roomMapper.toDto(room);
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
        if (reqIn == null || reqOut == null || !reqOut.isAfter(reqIn)) {
            return List.of();
        }

        Set<Long> occupiedRoomIds = new HashSet<>();
        try {
            List<Long> nativeOccupied = reservationRepository.findOccupiedRoomIdsNative(
                    companyId, reqIn, reqOut, excludeReservationId);
            if (nativeOccupied != null) {
                occupiedRoomIds.addAll(nativeOccupied);
            }
        } catch (Exception ignored) {
            // Native sorgu DB/enum farkında patlarsa entity yedek yolu kullanılır
        }

        // Yedek / birleştir: owning side rezervasyonlardan da doldur
        occupiedRoomIds.addAll(findOccupiedRoomIdsFromEntities(companyId, reqIn, reqOut, excludeReservationId));

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
            if (resIn.isBefore(reqOut) && resOut.isAfter(reqIn)) {
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
        Set<Long> occupied = new HashSet<>();
        try {
            occupied.addAll(reservationRepository.findOccupiedRoomIdsNative(
                    room.getCompany().getId(), reqIn, reqOut, excludeReservationId));
        } catch (Exception ignored) {
            // ignore
        }
        occupied.addAll(findOccupiedRoomIdsFromEntities(
                room.getCompany().getId(), reqIn, reqOut, excludeReservationId));
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
