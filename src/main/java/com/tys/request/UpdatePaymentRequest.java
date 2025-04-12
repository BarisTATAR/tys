package com.tys.request;
import com.tys.enums.PaymentType;
import com.tys.model.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePaymentRequest {

    private Long id;
    private BigDecimal amount;
    private BigDecimal advancePayment;
    private LocalDate paymentDate;
    private PaymentType paymentType;
    private Reservation reservation;

}
