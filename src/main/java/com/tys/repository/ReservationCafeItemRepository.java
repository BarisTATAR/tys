package com.tys.repository;

import com.tys.model.ReservationCafeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationCafeItemRepository extends JpaRepository<ReservationCafeItem, Long> {
}
