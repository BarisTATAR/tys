package com.tys.service;

import com.tys.dto.PaymentDto;
import com.tys.mapper.PaymentMapper;
import com.tys.model.Payment;
import com.tys.repository.PaymentRepository;
import com.tys.request.CreatePaymentRequest;
import com.tys.request.UpdatePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public void createPayment(CreatePaymentRequest request) {
        Payment payment = paymentMapper.createPaymentRequestToEntity(request);
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
}
