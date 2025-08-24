package com.tys.dto;

import com.tys.enums.PaymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class PaymentDto {
    private Long id;
    private BigDecimal amount;
    private BigDecimal advancePayment;
    private LocalDate paymentDate;
    private PaymentType paymentType;
    private Long reservationId;
}
