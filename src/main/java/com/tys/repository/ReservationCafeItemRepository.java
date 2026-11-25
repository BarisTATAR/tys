package com.tys.repository;

import com.tys.model.ReservationCafeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationCafeItemRepository extends JpaRepository<ReservationCafeItem, Long> {

    List<ReservationCafeItem> findAllByReservation_Id(Long reservationId);

    /** Aynı ürünün farklı fiyatla eklenmiş birden çok satırı olabilir: en son satır. */
    Optional<ReservationCafeItem> findFirstByReservation_IdAndCafe_IdOrderByIdDesc(Long reservationId, Long cafeId);

    /** Aynı ürün aynı fiyatla tekrar eklenirse adet bu satırda artırılır. */
    Optional<ReservationCafeItem> findFirstByReservation_IdAndCafe_IdAndUnitPrice(Long reservationId, Long cafeId, java.math.BigDecimal unitPrice);

    boolean existsByCafe_Id(Long cafeId);
}
