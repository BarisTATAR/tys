package com.tys.repository;

import com.tys.model.Company;
import com.tys.model.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Belirtilen tarih aralığında rezerve edilmemiş odaları getiren sorgu
    @Query("SELECT r FROM Room r WHERE r.id NOT IN (" +
            "SELECT room.id FROM Reservation res " +
            "JOIN res.rooms room " +
            "WHERE res.checkInDate < :checkOutDate AND res.checkOutDate > :checkInDate) " +
            "ORDER BY r.number ASC")
    List<Room> findAvailableRooms(@Param("checkInDate") LocalDateTime checkInDate, @Param("checkOutDate") LocalDateTime checkOutDate);

    // Belirtilen tarih aralığında ve işletmeye ait rezerve edilmemiş odaları getiren sorgu
    @Query("""
            SELECT r FROM Room r
            WHERE r.company.id = :companyId
            AND NOT EXISTS (
                SELECT 1 FROM Reservation res
                JOIN res.rooms occupiedRoom
                WHERE occupiedRoom = r
                AND res.company.id = :companyId
                AND res.reservationStatus NOT IN (com.tys.enums.ReservationStatus.CHECK_OUT_DONE, com.tys.enums.ReservationStatus.CANCELLED)
                AND res.checkInDate < :checkOutDate
                AND res.checkOutDate > :checkInDate
            )
            ORDER BY r.number ASC
            """)
    List<Room> findAvailableRoomsByCompanyId(@Param("checkInDate") LocalDateTime checkInDate, @Param("checkOutDate") LocalDateTime checkOutDate, @Param("companyId") Long companyId);

    /**
     * Güncellemede mevcut rezervasyon hariç tutulur (çıkarılan oda tekrar eklenebilsin).
     * Diğer rezervasyonlarla çakışan odalar dönmez.
     */
    @Query("""
            SELECT r FROM Room r
            WHERE r.company.id = :companyId
            AND NOT EXISTS (
                SELECT 1 FROM Reservation res
                JOIN res.rooms occupiedRoom
                WHERE occupiedRoom = r
                AND res.company.id = :companyId
                AND res.id <> :excludeReservationId
                AND res.reservationStatus NOT IN (com.tys.enums.ReservationStatus.CHECK_OUT_DONE, com.tys.enums.ReservationStatus.CANCELLED)
                AND res.checkInDate < :checkOutDate
                AND res.checkOutDate > :checkInDate
            )
            ORDER BY r.number ASC
            """)
    List<Room> findAvailableRoomsByCompanyIdExcludingReservation(
            @Param("checkInDate") LocalDateTime checkInDate,
            @Param("checkOutDate") LocalDateTime checkOutDate,
            @Param("companyId") Long companyId,
            @Param("excludeReservationId") Long excludeReservationId);

    @Query("SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.reservations")
    List<Room> findAllWithReservations();

    @Query("SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.reservations LEFT JOIN FETCH r.company WHERE r.company.id = :companyId")
    List<Room> findAllByCompanyIdWithReservations(@Param("companyId") Long companyId);

    Optional<Room> findByNumber(Integer number);

    Optional<Room> findByNumberAndCompanyId(Integer number, Long companyId);

    long countByCompany(Company company);



    boolean existsByNumberAndCompanyId(Integer number, Long companyId);

    boolean existsByNumberAndCompanyIdAndIdNot(Integer number, Long companyId, Long id);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.company WHERE r.company.id = :companyId ORDER BY r.number ASC")
    List<Room> findAllByCompanyIdOrdered(@Param("companyId") Long companyId);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.company ORDER BY r.company.id, r.number")
    List<Room> findAllOrdered();

    /**
     * Rezervasyon oluşturma/taşıma sırasında odaları satır kilidiyle (SELECT … FOR UPDATE) alır.
     * Aynı odaya eşzamanlı gelen istekler sıraya girer; müsaitlik kontrolü kilit alındıktan sonra yapılır,
     * böylece aynı gece iki kez satılamaz. Kilitler id sırasıyla alınır (kilitlenme/deadlock önlenir).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id IN :ids ORDER BY r.id")
    List<Room> lockAllByIdIn(@Param("ids") java.util.Collection<Long> ids);
}
