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
public class CreatePaidAmountRequest {
    private LocalDate paymentDate;
    private BigDecimal paidAmount;
    private PaymentType closePaymentType;
}
