package com.tys.repository;

import com.tys.dto.RoomDto;
import com.tys.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Belirtilen tarih aralığında rezerve edilmemiş odaları getiren sorgu
    @Query("SELECT r FROM Room r WHERE r.id NOT IN (" +
            "SELECT room.id FROM Reservation res " +
            "JOIN res.rooms room " +
            "WHERE res.checkInDate < :checkOutDate AND res.checkOutDate > :checkInDate)")
    List<Room> findAvailableRooms(@Param("checkInDate") LocalDateTime checkInDate, @Param("checkOutDate") LocalDateTime checkOutDate);
}