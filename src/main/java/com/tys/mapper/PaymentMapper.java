package com.tys.mapper;


import com.tys.model.Payment;
import com.tys.request.CreatePaymentRequest;
import com.tys.request.UpdatePaymentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment createPaymentRequestToEntity(CreatePaymentRequest createPaymentRequest);

    @Mapping(target = "id", ignore = true)
    void updateExistingPaymentWithPaymentRequest(UpdatePaymentRequest updatePaymentRequest, @MappingTarget Payment existingPayment);

}
