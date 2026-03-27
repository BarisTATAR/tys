package com.tys.mapper;

import com.tys.dto.PaymentDto;
import com.tys.model.Payment;
import com.tys.request.CreatePaymentRequest;
import com.tys.request.UpdatePaymentRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T16:20:26+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PaymentMapperImpl implements PaymentMapper {

    @Override
    public Payment createPaymentRequestToEntity(CreatePaymentRequest createPaymentRequest) {
        if ( createPaymentRequest == null ) {
            return null;
        }

        Payment payment = new Payment();

        payment.setAdvancePayment( createPaymentRequest.getAdvancePayment() );
        payment.setAdvancePaymentType( createPaymentRequest.getAdvancePaymentType() );
        payment.setAmount( createPaymentRequest.getAmount() );
        payment.setClosePaymentType( createPaymentRequest.getClosePaymentType() );
        payment.setPaymentDate( createPaymentRequest.getPaymentDate() );

        return payment;
    }

    @Override
    public void updateExistingPaymentWithPaymentRequest(UpdatePaymentRequest updatePaymentRequest, Payment existingPayment) {
        if ( updatePaymentRequest == null ) {
            return;
        }

        existingPayment.setAdvancePayment( updatePaymentRequest.getAdvancePayment() );
        existingPayment.setAdvancePaymentType( updatePaymentRequest.getAdvancePaymentType() );
        existingPayment.setAmount( updatePaymentRequest.getAmount() );
        existingPayment.setClosePaymentType( updatePaymentRequest.getClosePaymentType() );
        existingPayment.setId( updatePaymentRequest.getId() );
        existingPayment.setPaymentDate( updatePaymentRequest.getPaymentDate() );
    }

    @Override
    public PaymentDto toDto(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentDto paymentDto = new PaymentDto();

        paymentDto.setAdvancePayment( payment.getAdvancePayment() );
        paymentDto.setAdvancePaymentType( payment.getAdvancePaymentType() );
        paymentDto.setAmount( payment.getAmount() );
        paymentDto.setClosePaymentType( payment.getClosePaymentType() );
        paymentDto.setId( payment.getId() );
        paymentDto.setPaidAmount( payment.getPaidAmount() );
        paymentDto.setPaymentDate( payment.getPaymentDate() );

        return paymentDto;
    }
}
