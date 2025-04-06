package com.tys.request;
import com.tys.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePaymentRequest {

    private Long id;
    private double amount;
    private double advancePayment;
    private LocalDate paymentDate;
    private PaymentType paymentType;

}
