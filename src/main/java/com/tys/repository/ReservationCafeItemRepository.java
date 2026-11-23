package com.tys.repository;

import com.tys.model.ReservationCafeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationCafeItemRepository extends JpaRepository<ReservationCafeItem, Long> {

    List<ReservationCafeItem> findAllByReservation_Id(Long reservationId);

    Optional<ReservationCafeItem> findByReservation_IdAndCafe_Id(Long reservationId, Long cafeId);

    void deleteAllByCafe_Id(Long cafeId);
}
