package com.tys.repository;

import com.tys.model.Reservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.time.LocalDate;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"guests"})
    @Query("SELECT r FROM Reservation r WHERE r.checkOutDate >= CURRENT_DATE")
    List<Reservation> findAllWithGuests();

    @EntityGraph(attributePaths = {"guests"})
    @Query("SELECT r FROM Reservation r WHERE r.checkOutDate >= CURRENT_DATE AND r.company.id = :companyId")
    List<Reservation> findAllWithGuestsByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.rooms room LEFT JOIN FETCH room.company WHERE r.id IN :ids")
    List<Reservation> findWithRoomsByReservationIds(@Param("ids") Collection<Long> ids);

    /** İşletmeye ait rezervasyon id'leri (oda üzerinden de yakala) */
    @Query("""
            SELECT DISTINCT res.id FROM Reservation res
            LEFT JOIN res.rooms rm
            WHERE res.company.id = :companyId
               OR rm.company.id = :companyId
            """)
    List<Long> findReservationIdsByCompanyId(@Param("companyId") Long companyId);

    /**
     * Dolu oda id'leri — reservation_room üzerinden gün bazlı çakışma.
     * reservation_status Hibernate default ordinal: CHECK_OUT_DONE = 3
     */
    @Query(value = """
            SELECT DISTINCT rr.room_id
            FROM tys.reservation_room rr
            INNER JOIN tys.reservation res ON res.id = rr.reservation_id
            INNER JOIN tys.room rm ON rm.id = rr.room_id
            WHERE rm.company_id = :companyId
              AND (:excludeReservationId IS NULL OR res.id <> :excludeReservationId)
              AND (res.reservation_status IS NULL OR res.reservation_status <> 3)
              AND CAST(res.check_in_date AS date) < CAST(:checkOutDate AS date)
              AND CAST(res.check_out_date AS date) > CAST(:checkInDate AS date)
            """, nativeQuery = true)
    List<Long> findOccupiedRoomIdsNative(
            @Param("companyId") Long companyId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("excludeReservationId") Long excludeReservationId);
}
