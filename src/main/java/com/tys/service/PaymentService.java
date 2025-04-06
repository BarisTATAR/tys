package com.tys.service;

import com.tys.mapper.PaymentMapper;
import com.tys.model.Payment;
import com.tys.repository.PaymentRepository;
import com.tys.request.CreatePaymentRequest;
import com.tys.request.UpdatePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found with Id: " + id));
    }
}
