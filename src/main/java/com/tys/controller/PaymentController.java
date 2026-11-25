package com.tys.controller;


import com.tys.dto.PaymentDto;
import com.tys.enums.PaymentType;
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

    // Ödeme oluşturma: ana ödeme rezervasyon kaydıyla, ek ödeme POST /reservation/{id}/payments ile yapılır.
    // Doğrulamasız ve iş kurallarını atlayan eski POST /payment/create ucu kaldırıldı.

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
