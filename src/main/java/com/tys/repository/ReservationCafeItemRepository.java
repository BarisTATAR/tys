package com.tys.repository;

import com.tys.model.ReservationCafeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationCafeItemRepository extends JpaRepository<ReservationCafeItem, Long> {
        List<ReservationCafeItem> findAllByReservationId(Long reservationId);

}
