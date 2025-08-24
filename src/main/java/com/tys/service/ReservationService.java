package com.tys.service;

import com.tys.dto.ReservationDto;
import com.tys.mapper.GuestMapper;
import com.tys.mapper.ReservationMapper;
import com.tys.model.Reservation;
import com.tys.model.Room;
import com.tys.model.Guest;
import com.tys.repository.ReservationRepository;
import com.tys.repository.RoomRepository;
import com.tys.request.CreateReservationRequest;
import com.tys.request.CreateRoomRequest;
import com.tys.request.DeleteReservationRequest;
import com.tys.request.UpdateReservationRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final GuestMapper guestMapper;
    private final RoomRepository roomRepository;
    public void updateReservation(UpdateReservationRequest request) {

        Reservation existingReservation = reservationRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Reservation not found with Id: " + request.getId()));
        reservationMapper.updateExistingReservationWithReservationRequest(request, existingReservation);
        reservationRepository.save(existingReservation);
    }
    @Transactional
    public void createReservation(CreateReservationRequest request) {
        Reservation reservation = reservationMapper.createReservationRequestToEntity(request);

        List<Room> roomsFromDb = new ArrayList<>();
        for (CreateRoomRequest roomFromRequest : request.getRoomList()) {
            roomRepository.findByNumber(roomFromRequest.getNumber())
                    .ifPresent(roomsFromDb::add);
        }
        reservation.setRooms(roomsFromDb);

        List<Guest> guests = guestMapper.toEntityList(request.getGuestList());
        reservation.setGuests(guests);

        reservationRepository.save(reservation);
    }

    public void deleteReservation(DeleteReservationRequest request) {
        if (!reservationRepository.existsById(request.getId())) {
            throw new RuntimeException("Reservation not found with Id: " + request.getId());
        }
        reservationRepository.deleteById(request.getId());
    }

    public ReservationDto getReservationById(Long id) {
        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with Id: " + id));

        return reservationMapper.toDto(reservation);
    }

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public List<ReservationDto> getAllWithGuests() {
        List<Reservation> reservations = reservationRepository.findAllWithGuests();
        return reservations.stream()
                .map(reservationMapper::toDto)
                .toList();
    }

}