package com.tys.request;

import com.tys.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentRequest {
    private Long id;
    private BigDecimal amount;
    private BigDecimal advancePayment;
    private LocalDate paymentDate;
    private PaymentType advancePaymentType;
    private PaymentType closePaymentType;
    private Long reservationId;

}
