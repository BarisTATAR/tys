package com.tys.repository;

import com.tys.model.Reservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @EntityGraph(attributePaths = {"guests"})
    @Query("SELECT r FROM Reservation r WHERE r.checkOutDate >= CURRENT_DATE")
    List<Reservation> findAllWithGuests();

    @EntityGraph(attributePaths = {"guests"})
    @Query("SELECT r FROM Reservation r WHERE r.checkOutDate >= CURRENT_DATE AND r.company.id = :companyId")
    List<Reservation> findAllWithGuestsByCompanyId(@Param("companyId") Long companyId);
}