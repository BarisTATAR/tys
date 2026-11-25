package com.tys.service;

import com.tys.dto.GuestDto;
import com.tys.dto.ReservationDto;
import com.tys.dto.RoomDto;
import com.tys.enums.ReservationStatus;
import com.tys.mapper.RoomMapper;
import com.tys.model.Company;
import com.tys.model.Guest;
import com.tys.model.Reservation;
import com.tys.model.Room;
import com.tys.repository.CompanyRepository;
import com.tys.repository.ReservationRepository;
import com.tys.repository.RoomRepository;
import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteRoomRequest;
import com.tys.request.UpdateRoomRequest;
import com.tys.util.ReservationDateUtils;
import com.tys.validation.InputRules;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final CompanyRepository companyRepository;
    private final RoomMapper roomMapper;

    @Transactional
    public void createRoom(CreateRoomRequest request, Long actorCompanyId, boolean admin) {
        if (request == null) {
            throw new IllegalArgumentException("Oda bilgileri gereklidir.");
        }
        InputRules.room(request.getNumber(), request.getCapacity(), request.getFloor());
        Company company = resolveCompany(request, actorCompanyId, admin);
        if (roomRepository.existsByNumberAndCompanyId(request.getNumber(), company.getId())) {
            throw new IllegalArgumentException("Bu işletmede " + request.getNumber() + " numaralı oda zaten kayıtlı.");
        }
        long currentRoomCount = roomRepository.countByCompany(company);
        Integer limit = company.getTotalRoomNumber();
        if (limit == null || currentRoomCount >= limit) {
            throw new IllegalArgumentException("Toplam oda sayısı sınırı aşıldı. Maksimum izin verilen oda sayısı: "
                    + (limit == null ? 0 : limit));
        }
        Room room = roomMapper.createRoomRequestToEntity(request);
        room.setCompany(company);
        roomRepository.save(room);
    }

    /** Rezervasyon geçmişi olan oda silinmez (rezervasyon kayıtları bozulur); 409 döner. */
    @Transactional
    public void deleteRoom(DeleteRoomRequest request, Long actorCompanyId, boolean admin) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Silinecek oda seçilmelidir.");
        }
        Room room = roomRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oda bulunamadı."));
        assertRoomCompany(room, actorCompanyId, admin);
        if (room.getReservations() != null && !room.getReservations().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Oda " + room.getNumber() + " rezervasyon kayıtlarında kullanıldığı için silinemez.");
        }
        roomRepository.delete(room);
    }

    @Transactional
    public void updateRoom(UpdateRoomRequest request, Long actorCompanyId, boolean admin) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Güncellenecek oda seçilmelidir.");
        }
        Room existingRoom = roomRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oda bulunamadı."));
        assertRoomCompany(existingRoom, actorCompanyId, admin);
        InputRules.room(request.getNumber(), request.getCapacity(), request.getFloor());
        Company owner = existingRoom.getCompany();
        if (owner != null && roomRepository.existsByNumberAndCompanyIdAndIdNot(request.getNumber(), owner.getId(), existingRoom.getId())) {
            throw new IllegalArgumentException("Bu işletmede " + request.getNumber() + " numaralı başka bir oda var.");
        }
        roomMapper.updateExistingRoomWithRoomRequest(request, existingRoom);
        existingRoom.setCompany(owner);
        roomRepository.save(existingRoom);
    }

    @Transactional(readOnly = true)
    public RoomDto getRoomById(Long id, Long actorCompanyId, boolean admin) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oda bulunamadı."));
        assertRoomCompany(room, actorCompanyId, admin);
        return toPlainRoomDto(room);
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

    /** Admin: tüm işletmelerin odaları (açık rezervasyonlar sade biçimde). */
    @Transactional(readOnly = true)
    public List<RoomDto> getAllRooms() {
        List<Room> rooms = roomRepository.findAllOrdered();
        Map<Long, List<ReservationDto>> open = new HashMap<>();
        rooms.stream()
                .map(room -> room.getCompany() == null ? null : room.getCompany().getId())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .forEach(companyId -> open.putAll(openReservationsByRoom(companyId)));
        return rooms.stream().map(room -> toBoardRoomDto(room, open)).toList();
    }

    @Transactional(readOnly = true)
    public List<RoomDto> getAvailableRooms(LocalDateTime checkInDate, LocalDateTime checkOutDate, Long companyId) {
        return getAvailableRoomsExcludingReservation(checkInDate, checkOutDate, companyId, null);
    }

    /**
     * Seçilen tarihlerde müsait odalar.
     * 1) SQL ile dolu oda id'leri
     * 2) Yedek: yalnız tarih penceresindeki rezervasyonlar üzerinden gün bazlı çakışma
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

        Set<Long> occupiedRoomIds = new HashSet<>(
                reservationRepository.findOccupiedRoomIdsNative(companyId, reqIn, queryOut, excludeReservationId));
        occupiedRoomIds.addAll(findOccupiedRoomIdsFromEntities(companyId, reqIn, queryOut, excludeReservationId));

        return roomRepository.findAllByCompanyIdOrdered(companyId).stream()
                .filter(room -> room.getId() != null && !occupiedRoomIds.contains(room.getId()))
                .sorted(Comparator.comparing(Room::getNumber, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toPlainRoomDto)
                .toList();
    }

    private Set<Long> findOccupiedRoomIdsFromEntities(
            Long companyId,
            LocalDate reqIn,
            LocalDate reqOut,
            Long excludeReservationId) {

        Set<Long> occupied = new HashSet<>();
        // Aynı gün rezervasyonlar [in, in+1) sayıldığından pencere bir gün geniş tutulur
        List<Reservation> candidates = reservationRepository.findOccupancyCandidates(
                companyId, reqIn.minusDays(1).atStartOfDay(), reqOut.atStartOfDay());
        for (Reservation res : candidates) {
            if (res == null) {
                continue;
            }
            if (excludeReservationId != null && excludeReservationId.equals(res.getId())) {
                continue;
            }
            if (!occupiesRooms(res)) {
                continue;
            }
            LocalDate resIn = ReservationDateUtils.toLocalDate(res.getCheckInDate());
            LocalDate resOut = ReservationDateUtils.toLocalDate(res.getCheckOutDate());
            if (resIn == null || resOut == null) {
                continue;
            }
            LocalDate effectiveResOut = resOut.isEqual(resIn) ? resIn.plusDays(1) : resOut;
            if (resIn.isBefore(reqOut) && effectiveResOut.isAfter(reqIn) && res.getRooms() != null) {
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
        Set<Long> occupied = new HashSet<>(reservationRepository.findOccupiedRoomIdsNative(
                room.getCompany().getId(), reqIn, queryOut, excludeReservationId));
        occupied.addAll(findOccupiedRoomIdsFromEntities(
                room.getCompany().getId(), reqIn, queryOut, excludeReservationId));
        return !occupied.contains(room.getId());
    }

    /**
     * Oda tahtası: işletmenin odaları ve odayı işgal eden açık rezervasyonlar (check-out/iptal hariç).
     * Rezervasyon geçmişi ve misafirlerin kimlik/iletişim bilgileri dönmez; detay için /reservation/{id} kullanılır.
     */
    @Transactional(readOnly = true)
    public List<RoomDto> getAllRoomsByCompanyId(Long companyId) {
        Map<Long, List<ReservationDto>> open = openReservationsByRoom(companyId);
        return roomRepository.findAllByCompanyIdOrdered(companyId).stream()
                .map(room -> toBoardRoomDto(room, open))
                .toList();
    }

    private Map<Long, List<ReservationDto>> openReservationsByRoom(Long companyId) {
        Map<Long, List<ReservationDto>> byRoom = new HashMap<>();
        for (Reservation reservation : reservationRepository.findOpenWithRoomsByCompanyId(companyId)) {
            if (reservation.getRooms() == null) {
                continue;
            }
            ReservationDto dto = toBoardReservationDto(reservation);
            for (Room room : reservation.getRooms()) {
                if (room != null && room.getId() != null) {
                    byRoom.computeIfAbsent(room.getId(), key -> new ArrayList<>()).add(dto);
                }
            }
        }
        return byRoom;
    }

    private boolean occupiesRooms(Reservation reservation) {
        ReservationStatus status = reservation.getReservationStatus();
        return status != ReservationStatus.CHECK_OUT_DONE && status != ReservationStatus.CANCELLED;
    }

    private RoomDto toBoardRoomDto(Room room, Map<Long, List<ReservationDto>> openReservations) {
        RoomDto dto = toPlainRoomDto(room);
        dto.setReservations(openReservations.getOrDefault(room.getId(), List.of()));
        return dto;
    }

    private ReservationDto toBoardReservationDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getId());
        dto.setAdultGuestNumber(reservation.getAdultGuestNumber());
        dto.setChildGuestNumber(reservation.getChildGuestNumber());
        dto.setBabyGuestNumber(reservation.getBabyGuestNumber());
        dto.setCheckInDate(ReservationDateUtils.toLocalDate(reservation.getCheckInDate()));
        dto.setCheckOutDate(ReservationDateUtils.toLocalDate(reservation.getCheckOutDate()));
        dto.setReservationMessage(reservation.getReservationMessage());
        dto.setReservationType(reservation.getReservationType());
        dto.setReservationStatus(reservation.getReservationStatus());
        dto.setReservationDate(reservation.getReservationDate());
        List<GuestDto> guests = new ArrayList<>();
        if (reservation.getGuests() != null) {
            for (Guest guest : reservation.getGuests()) {
                GuestDto g = new GuestDto();
                g.setId(guest.getId());
                g.setName(guest.getName());
                g.setSurname(guest.getSurname());
                g.setIsContact(guest.getIsContact());
                guests.add(g);
            }
        }
        dto.setGuestList(guests);
        List<RoomDto> rooms = new ArrayList<>();
        for (Room room : reservation.getRooms()) {
            RoomDto r = new RoomDto();
            r.setId(room.getId());
            r.setNumber(room.getNumber());
            r.setCapacity(room.getCapacity());
            r.setFloor(room.getFloor());
            rooms.add(r);
        }
        rooms.sort(Comparator.comparing(RoomDto::getNumber, Comparator.nullsLast(Integer::compareTo)));
        dto.setRoomList(rooms);
        return dto;
    }

    private RoomDto toPlainRoomDto(Room room) {
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
}
