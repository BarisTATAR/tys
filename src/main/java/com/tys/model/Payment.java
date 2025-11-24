package com.tys.model;


import com.tys.enums.PaymentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(schema = "tys", name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "advance payment")
    private BigDecimal advancePayment;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "advance_payment_type")
    private PaymentType advancePaymentType;

    @Column(name = "close_payment_type")
    private PaymentType closePaymentType;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

}
