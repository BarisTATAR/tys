package com.tys.service;

import com.tys.enums.PaymentType;
import com.tys.mapper.PaymentMapper;
import com.tys.model.Payment;
import com.tys.repository.PaymentRepository;
import com.tys.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private static Payment row(String amount, String advance, PaymentType advanceType, String paid, PaymentType closeType) {
        Payment p = new Payment();
        p.setAmount(amount == null ? null : new BigDecimal(amount));
        p.setAdvancePayment(advance == null ? null : new BigDecimal(advance));
        p.setAdvancePaymentType(advanceType);
        p.setPaidAmount(paid == null ? null : new BigDecimal(paid));
        p.setClosePaymentType(closeType);
        return p;
    }

    @Test
    void bolunmusKaporaVeEkOdemelerOdenenTutaraGirer() {
        List<Payment> rows = List.of(
                row("1000", "300", PaymentType.CASH, null, null),
                row(null, "200", PaymentType.CREDIT_CARD, null, null),
                row(null, null, null, "150", PaymentType.DEBIT_CARD));
        assertThat(PaymentService.totalAdvance(rows)).isEqualByComparingTo("500");
        assertThat(PaymentService.totalPaid(rows)).isEqualByComparingTo("650");
    }

    @Test
    void odemeSekliRaporundaHerTutarYalnizKendiSeklindeSayilir() {
        PaymentRepository repo = Mockito.mock(PaymentRepository.class);
        PaymentMapper mapper = Mockito.mock(PaymentMapper.class, Mockito.CALLS_REAL_METHODS);
        PaymentService service = new PaymentService(repo, Mockito.mock(ReservationRepository.class), mapper);
        // Ana satır: kapora nakit, kapanış şekli kart (tutar yok); ek ödeme kartla
        List<Payment> rows = List.of(
                row("700", "300", PaymentType.CASH, null, PaymentType.CREDIT_CARD),
                row(null, null, null, "400", PaymentType.CREDIT_CARD));
        LocalDate day = LocalDate.of(2026, 11, 25);
        when(repo.findByDateRangeAndCompanyId(eq(day), eq(day), any())).thenReturn(rows);

        assertThat(service.getPayments(day, day, PaymentType.CASH, 1L)).isEqualByComparingTo("300");
        assertThat(service.getPayments(day, day, PaymentType.CREDIT_CARD, 1L)).isEqualByComparingTo("400");
        assertThat(service.getPayments(day, day, PaymentType.ALL, 1L)).isEqualByComparingTo("700");
    }
}
