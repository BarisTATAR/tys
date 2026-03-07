package com.tys.repository;

import com.tys.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    Optional<Guest> findByIdentityNumber(String identityNumber);

    @Query("SELECT g FROM Guest g WHERE g.checkInDate BETWEEN :start AND :end")
    List<Guest> findAllByCheckInYear(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Query("SELECT g FROM Guest g WHERE g.company.id = :companyId AND g.checkInDate BETWEEN :start AND :end")
    List<Guest> findAllByCheckInYearAndCompanyId(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end,
                                                 @Param("companyId") Long companyId);

    @Query("SELECT DISTINCT g FROM Guest g LEFT JOIN g.reservations r " +
           "WHERE g.company.id = :companyId OR r.company.id = :companyId")
    List<Guest> findAllByCompanyId(@Param("companyId") Long companyId);

}