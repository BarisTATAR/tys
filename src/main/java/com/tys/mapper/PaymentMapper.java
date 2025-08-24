package com.tys.mapper;


import com.tys.dto.PaymentDto;
import com.tys.model.Payment;
import com.tys.request.CreatePaymentRequest;
import com.tys.request.UpdatePaymentRequest;
import org.mapstruct.*;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {
    Payment createPaymentRequestToEntity(CreatePaymentRequest createPaymentRequest);

    void updateExistingPaymentWithPaymentRequest(UpdatePaymentRequest updatePaymentRequest, @MappingTarget Payment existingPayment);

    PaymentDto toDto(Payment payment);
}
