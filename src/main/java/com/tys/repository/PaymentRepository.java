package com.tys.repository;

import com.tys.enums.PaymentType;
import com.tys.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByReservationId(Long reservationId);

    @Query("""
            SELECT p
            FROM Payment p
            WHERE p.paymentDate BETWEEN :startDate AND :endDate
            """)
    List<Payment> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT p
            FROM Payment p
            JOIN p.reservation r
            WHERE r.company.id = :companyId
            AND p.paymentDate BETWEEN :startDate AND :endDate
            """)
    List<Payment> findByDateRangeAndCompanyId(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("companyId") Long companyId);

    @Query("""
            SELECT p
            FROM Payment p
            WHERE p.paymentDate BETWEEN :startDate AND :endDate
            AND (
                 p.advancePaymentType = :paymentType
                 OR p.closePaymentType = :paymentType
            )
            """)
    List<Payment> findByDateRangeAndPaymentType(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("paymentType") PaymentType paymentType);

    @Query("""
            SELECT p
            FROM Payment p
            JOIN p.reservation r
            WHERE r.company.id = :companyId
            AND p.paymentDate BETWEEN :startDate AND :endDate
            AND (
                 p.advancePaymentType = :paymentType
                 OR p.closePaymentType = :paymentType
            )
            """)
    List<Payment> findByDateRangeAndPaymentTypeAndCompanyId(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("paymentType") PaymentType paymentType, @Param("companyId") Long companyId);

}
