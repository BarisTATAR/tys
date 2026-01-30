package com.tys.repository;

import com.tys.model.Company;
import com.tys.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.reservations")
    List<Room> findAllWithReservations();

    @Query("SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.reservations LEFT JOIN FETCH r.company WHERE r.company.id = :companyId")
    List<Room> findAllByCompanyIdWithReservations(@Param("companyId") Long companyId);

    Optional<Room> findByNumber(Integer number);

    long countByCompany(Company company);


}