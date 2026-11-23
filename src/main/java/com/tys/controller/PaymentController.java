package com.tys.controller;


import com.tys.dto.PaymentDto;
import com.tys.enums.PaymentType;
import com.tys.model.Payment;
import com.tys.request.CreatePaymentRequest;
import com.tys.request.UpdatePaymentRequest;
import com.tys.security.CurrentActor;
import com.tys.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentActor currentActor;

    private Long getCurrentCompanyIdFromAuth() {
        return currentActor.requireCompanyId();
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createPayment(@RequestBody CreatePaymentRequest request) {
        Long companyId = getCurrentCompanyIdFromAuth();
        paymentService.createPayment(request, companyId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updatePayment(@RequestBody UpdatePaymentRequest request) {
        paymentService.updatePayment(request, getCurrentCompanyIdFromAuth());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(paymentService.getPaymentByReservationId(id, getCurrentCompanyIdFromAuth()));
    }

    @GetMapping
    public BigDecimal getPayments(@RequestParam(name = "startDate") LocalDate startDate, @RequestParam(name = "endDate") LocalDate endDate, @RequestParam(name = "paymentType") PaymentType paymentType) {
        Long companyId = getCurrentCompanyIdFromAuth();
        return paymentService.getPayments(startDate, endDate, paymentType, companyId);
    }
}
