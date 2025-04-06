package com.tys.request;
import com.tys.enums.CompanyCategory;
import com.tys.enums.PaymentType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentRequest {

    private Long id;
    private double amount;
    private double advancePayment;
    private LocalDate paymentDate;
    private PaymentType paymentType;
}
