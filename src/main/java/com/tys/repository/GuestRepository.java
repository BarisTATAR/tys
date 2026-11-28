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

    boolean existsByIdentityNumber(String identityNumber);

    @Query("SELECT g FROM Guest g WHERE g.checkInDate BETWEEN :start AND :end")
    List<Guest> findAllByCheckInYear(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    @Query("""
            SELECT DISTINCT g FROM Guest g
            LEFT JOIN g.reservations r
            WHERE (g.company.id = :companyId OR r.company.id = :companyId)
            AND (
                g.checkInDate BETWEEN :start AND :end
                OR r.checkInDate BETWEEN :start AND :end
            )
            """)
    List<Guest> findAllByCheckInYearAndCompanyId(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end,
                                                 @Param("companyId") Long companyId);

    @Query("SELECT DISTINCT g FROM Guest g LEFT JOIN g.reservations r " +
           "WHERE g.company.id = :companyId OR r.company.id = :companyId")
    List<Guest> findAllByCompanyId(@Param("companyId") Long companyId);

    @Query("""
            SELECT DISTINCT g FROM Guest g
            LEFT JOIN FETCH g.reservations r
            LEFT JOIN FETCH r.company
            WHERE g.company.id = :companyId OR r.company.id = :companyId
            """)
    List<Guest> findAllByCompanyIdWithReservations(@Param("companyId") Long companyId);


    long countByCompany_Id(Long companyId);

    /** İşletmeye ait (işletme kaydı veya işletmenin rezervasyonu üzerinden) TCKN eşleşmeleri; en yeni kayıt önce. */
    @Query("""
            SELECT DISTINCT g FROM Guest g
            LEFT JOIN g.reservations r
            WHERE g.identityNumber = :identityNumber
              AND (g.company.id = :companyId OR (g.company IS NULL AND r.company.id = :companyId))
            ORDER BY g.id DESC
            """)
    List<Guest> findAllByIdentityNumberInCompany(@Param("identityNumber") String identityNumber,
                                                 @Param("companyId") Long companyId);

    /** İşletmenin misafirlerinde kayıtlı iller (SMS il filtresi). */
    @Query("""
            SELECT DISTINCT g.city FROM Guest g
            WHERE g.company.id = :companyId AND g.city IS NOT NULL AND g.city <> ''
            ORDER BY g.city
            """)
    List<String> findCitiesByCompanyId(@Param("companyId") Long companyId);

    @Query("""
            SELECT g FROM Guest g
            WHERE g.company.id = :companyId AND LOWER(g.city) = LOWER(:city)
            """)
    List<Guest> findAllByCompanyIdAndCity(@Param("companyId") Long companyId, @Param("city") String city);
}
