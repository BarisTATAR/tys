package com.tys.repository;

import com.tys.model.KbsNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KbsNoticeRepository extends JpaRepository<KbsNotice, Long> {
    Optional<KbsNotice> findFirstByReservationIdOrderByCreatedAtDesc(Long reservationId);
}
