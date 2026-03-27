package com.tys.service;

import com.tys.dto.PaymentDto;
import com.tys.enums.PaymentType;
import com.tys.mapper.PaymentMapper;
import com.tys.model.Payment;
import com.tys.model.Reservation;
import com.tys.repository.PaymentRepository;
import com.tys.repository.ReservationRepository;
import com.tys.request.CreatePaymentRequest;
import com.tys.request.UpdatePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentMapper paymentMapper;

    public void createPayment(CreatePaymentRequest request, Long companyId) {
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + request.getReservationId()));
        if (reservation.getCompany() == null || !reservation.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Bu rezervasyon için ödeme oluşturma yetkiniz yok.");
        }
        Payment payment = paymentMapper.createPaymentRequestToEntity(request);
        payment.setReservation(reservation);
        paymentRepository.save(payment);
    }

    public void updatePayment(UpdatePaymentRequest request) {
        Payment existingPayment = paymentRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Payment not found with Id: " + request.getId()));
        paymentMapper.updateExistingPaymentWithPaymentRequest(request, existingPayment);
        paymentRepository.save(existingPayment);
    }

    public PaymentDto getPaymentByReservationId(Long id) {
        List<Payment> payments = paymentRepository.findAllByReservationId(id);
        Payment payment = new Payment();

        if (payments.isEmpty()) {
            throw new RuntimeException("No payments found for reservationId: " + id);
        } else {
            payment.setAmount(payments.get(0).getAmount());
            payment.setAdvancePayment(payments.get(0).getAdvancePayment());
            BigDecimal paidAmount = payments.get(0).getAdvancePayment();
            for (Payment p : payments) {
                if (p.getPaidAmount() != null) {
                    paidAmount = paidAmount.add(p.getPaidAmount());
                }
            }
            payment.setPaidAmount(paidAmount);
        }

        return paymentMapper.toDto(payment);
    }

    public BigDecimal getPayments(LocalDate startDate, LocalDate endDate, PaymentType paymentType, Long companyId) {
        List<Payment> payments;
        if (paymentType == PaymentType.ALL) {
            payments = paymentRepository.findByDateRangeAndCompanyId(startDate, endDate, companyId);
        } else {
            payments = paymentRepository.findByDateRangeAndPaymentTypeAndCompanyId(startDate, endDate, paymentType, companyId);
        }
        return paymentMapper.calculateTotalAmount(payments);
    }
}
