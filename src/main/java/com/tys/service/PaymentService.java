package com.tys.service;

import com.tys.dto.PaymentDto;
import com.tys.enums.PaymentType;
import com.tys.enums.ReservationStatus;
import com.tys.mapper.PaymentMapper;
import com.tys.model.Payment;
import com.tys.model.Reservation;
import com.tys.repository.PaymentRepository;
import com.tys.repository.ReservationRepository;
import com.tys.request.UpdatePaymentRequest;
import com.tys.validation.InputRules;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ödeme kayıtları. Bir rezervasyonun ilk ödeme satırı ana ödemedir (toplam tutar + kapora);
 * sonraki satırlar bölünmüş kapora veya ek ödeme (tahsilat) olabilir.
 * Ödenen tutar = tüm satırlardaki kaporalar + tüm ek ödemeler.
 * Ödeme oluşturma rezervasyon kaydında (ana ödeme) ve /reservation/{id}/payments ucunda (ek ödeme) yapılır.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public void updatePayment(UpdatePaymentRequest request, Long companyId) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Güncellenecek ödeme seçilmelidir.");
        }
        Payment existingPayment = paymentRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ödeme kaydı bulunamadı."));
        assertPaymentCompany(existingPayment, companyId);
        if (existingPayment.getReservation().getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("İptal edilmiş rezervasyonun ödemesi değiştirilemez.");
        }
        BigDecimal amount = request.getAmount() != null ? request.getAmount() : existingPayment.getAmount();
        BigDecimal advance = request.getAdvancePayment() != null ? request.getAdvancePayment() : existingPayment.getAdvancePayment();
        InputRules.payment(amount, advance);
        PaymentType advanceType = request.getAdvancePaymentType() != null ? request.getAdvancePaymentType() : existingPayment.getAdvancePaymentType();
        if (advance != null && advance.signum() > 0 && advanceType == null) {
            throw new IllegalArgumentException("Kapora için ödeme şekli seçilmelidir.");
        }
        paymentMapper.updateExistingPaymentWithPaymentRequest(request, existingPayment);
        paymentRepository.save(existingPayment);
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentByReservationId(Long id, Long companyId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervasyon bulunamadı."));
        if (reservation.getCompany() == null || !companyId.equals(reservation.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
        }
        List<Payment> payments = paymentRepository.findAllByReservationIdOrderByIdAsc(id);
        PaymentDto dto = new PaymentDto();
        dto.setReservationId(id);

        if (payments.isEmpty()) {
            dto.setAmount(BigDecimal.ZERO);
            dto.setAdvancePayment(BigDecimal.ZERO);
            dto.setPaidAmount(BigDecimal.ZERO);
            return dto;
        }

        Payment first = payments.get(0);
        dto.setId(first.getId());
        dto.setAmount(first.getAmount() != null ? first.getAmount() : BigDecimal.ZERO);
        dto.setAdvancePaymentType(first.getAdvancePaymentType());
        dto.setClosePaymentType(first.getClosePaymentType());
        dto.setPaymentDate(first.getPaymentDate());
        dto.setAdvancePayment(totalAdvance(payments));
        dto.setPaidAmount(totalPaid(payments));
        return dto;
    }

    private void assertPaymentCompany(Payment payment, Long companyId) {
        Reservation reservation = payment.getReservation();
        if (reservation == null || reservation.getCompany() == null || !companyId.equals(reservation.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
        }
    }

    /**
     * Tarih aralığındaki tahsilat. Ödeme şekline göre sorguda her tutar yalnız kendi şekline sayılır:
     * kapora, kapora ödeme şekline; ek ödeme, kapanış ödeme şekline.
     */
    @Transactional(readOnly = true)
    public BigDecimal getPayments(LocalDate startDate, LocalDate endDate, PaymentType paymentType, Long companyId) {
        if (startDate == null || endDate == null || paymentType == null) {
            throw new IllegalArgumentException("Başlangıç, bitiş tarihi ve ödeme şekli gereklidir.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Bitiş tarihi başlangıç tarihinden önce olamaz.");
        }
        List<Payment> payments = paymentRepository.findByDateRangeAndCompanyId(startDate, endDate, companyId);
        if (paymentType == PaymentType.ALL) {
            return paymentMapper.calculateTotalAmount(payments);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Payment p : payments) {
            if (p.getAdvancePaymentType() == paymentType) {
                total = total.add(safe(p.getAdvancePayment()));
            }
            if (p.getClosePaymentType() == paymentType) {
                total = total.add(safe(p.getPaidAmount()));
            }
        }
        return total;
    }

    /** Tüm satırlardaki kapora toplamı. */
    public static BigDecimal totalAdvance(List<Payment> payments) {
        BigDecimal sum = BigDecimal.ZERO;
        if (payments != null) {
            for (Payment p : payments) {
                sum = sum.add(safe(p.getAdvancePayment()));
            }
        }
        return sum;
    }

    /** Ödenen = tüm kaporalar + tüm ek ödemeler (check-out borç hesabı ve ekrandaki özet aynı kuralı kullanır). */
    public static BigDecimal totalPaid(List<Payment> payments) {
        BigDecimal sum = totalAdvance(payments);
        if (payments != null) {
            for (Payment p : payments) {
                sum = sum.add(safe(p.getPaidAmount()));
            }
        }
        return sum;
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
