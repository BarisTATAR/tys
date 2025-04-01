package com.tys.service;


import com.tys.mapper.ReservationMapper;
import com.tys.model.Company;
import com.tys.model.Reservation;
import com.tys.repository.ReservationRepository;
import com.tys.request.CreateReservationRequest;
import com.tys.request.DeleteReservationRequest;
import com.tys.request.UpdateReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public void updateReservation(UpdateReservationRequest request) {

        Reservation existingReservation = reservationRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Company not found with Id: " + request.getId()));
        reservationMapper.updateExistingReservationWithReservationRequest(request, existingReservation);
        reservationRepository.save(existingReservation);
    }

    public void createReservation(CreateReservationRequest request) {
        Reservation reservation = reservationMapper.createReservationRequestToEntity(request);
        reservationRepository.save(reservation);
    }

    public void deleteReservation(DeleteReservationRequest request) {
        if (!reservationRepository.existsById(request.getId())) {
            throw new RuntimeException("Reservation not found with Id: " + request.getId());
        }
        reservationRepository.deleteById(request.getId());
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new RuntimeException("Reservation not found with Id: " + id));
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

}
