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
    @Query("SELECT r FROM Reservation r WHERE r.checkOutDate >= CURRENT_DATE AND r.company.id = :companyId " +
            "AND (r.reservationStatus IS NULL OR r.reservationStatus <> com.tys.enums.ReservationStatus.CANCELLED)")
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
     * Check-out yapılmış ve iptal edilmiş rezervasyonlar odayı işgal etmez (durumlar metin olarak saklanır).
     */
    @Query(value = """
            SELECT DISTINCT rr.room_id
            FROM tys.reservation_room rr
            INNER JOIN tys.reservation res ON res.id = rr.reservation_id
            INNER JOIN tys.room rm ON rm.id = rr.room_id
            WHERE rm.company_id = :companyId
              AND (:excludeReservationId IS NULL OR res.id <> :excludeReservationId)
              AND (res.reservation_status IS NULL OR res.reservation_status NOT IN ('CHECK_OUT_DONE', 'CANCELLED'))
              AND CAST(res.check_in_date AS date) < CAST(:checkOutDate AS date)
              AND (
                    CASE
                        WHEN CAST(res.check_out_date AS date) = CAST(res.check_in_date AS date)
                            THEN CAST(res.check_in_date AS date) + INTERVAL '1 day'
                        ELSE CAST(res.check_out_date AS date)
                    END
                  ) > CAST(:checkInDate AS date)
            """, nativeQuery = true)
    List<Long> findOccupiedRoomIdsNative(
            @Param("companyId") Long companyId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("excludeReservationId") Long excludeReservationId);

    long countByCompany_Id(Long companyId);

    /** Oda tahtası: odaları işgal eden (check-out/iptal olmamış) rezervasyonlar, odaları ve misafirleriyle. */
    @Query("""
            SELECT DISTINCT r FROM Reservation r
            LEFT JOIN FETCH r.rooms room
            WHERE r.company.id = :companyId
              AND (r.reservationStatus IS NULL
                   OR r.reservationStatus NOT IN (com.tys.enums.ReservationStatus.CHECK_OUT_DONE, com.tys.enums.ReservationStatus.CANCELLED))
            """)
    List<Reservation> findOpenWithRoomsByCompanyId(@Param("companyId") Long companyId);

    /** Müsaitlik yedek kontrolü için tarih penceresiyle sınırlı aday rezervasyonlar (tüm geçmiş yüklenmez). */
    @Query("""
            SELECT DISTINCT r FROM Reservation r
            LEFT JOIN FETCH r.rooms room
            WHERE r.company.id = :companyId
              AND r.checkInDate < :windowEnd
              AND r.checkOutDate >= :windowStart
              AND (r.reservationStatus IS NULL
                   OR r.reservationStatus NOT IN (com.tys.enums.ReservationStatus.CHECK_OUT_DONE, com.tys.enums.ReservationStatus.CANCELLED))
            """)
    List<Reservation> findOccupancyCandidates(@Param("companyId") Long companyId,
                                              @Param("windowStart") java.time.LocalDateTime windowStart,
                                              @Param("windowEnd") java.time.LocalDateTime windowEnd);
}
