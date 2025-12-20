package com.tys.controller;


import com.tys.dto.PaymentDto;
import com.tys.enums.PaymentType;
import com.tys.model.Guest;
import com.tys.model.Payment;
import com.tys.request.CreatePaymentRequest;
import com.tys.request.UpdatePaymentRequest;
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
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<Void> createPayment(@RequestBody CreatePaymentRequest request) {
        paymentService.createPayment(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updatePayment(@RequestBody UpdatePaymentRequest request) {
        paymentService.updatePayment(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(paymentService.getPaymentByReservationId(id));
    }

    @GetMapping
    public BigDecimal getPayments(@RequestParam(name = "startDate") LocalDate startDate, @RequestParam(name = "endDate") LocalDate endDate, @RequestParam(name = "paymentType") PaymentType paymentType) {
        return paymentService.getPayments(startDate, endDate, paymentType);
    }
}
