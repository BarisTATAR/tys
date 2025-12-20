package com.tys.mapper;


import com.tys.dto.PaymentDto;
import com.tys.model.Payment;
import com.tys.request.CreatePaymentRequest;
import com.tys.request.UpdatePaymentRequest;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {
    Payment createPaymentRequestToEntity(CreatePaymentRequest createPaymentRequest);

    void updateExistingPaymentWithPaymentRequest(UpdatePaymentRequest updatePaymentRequest, @MappingTarget Payment existingPayment);

    PaymentDto toDto(Payment payment);

    default BigDecimal calculateTotalAmount(List<Payment> payments) {
        return payments.stream().map(p -> safe(p.getPaidAmount()).add(safe(p.getAdvancePayment()))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

}
