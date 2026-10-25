package com.tys.repository;

import com.tys.model.RulesApprovalLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RulesApprovalLinkRepository extends JpaRepository<RulesApprovalLink, Long> {
    Optional<RulesApprovalLink> findByToken(String token);

    List<RulesApprovalLink> findAllByReservationIdOrderByCreatedAtDesc(Long reservationId);

    Optional<RulesApprovalLink> findFirstByReservationIdAndApprovedTrueOrderByApprovedAtDesc(Long reservationId);
}
