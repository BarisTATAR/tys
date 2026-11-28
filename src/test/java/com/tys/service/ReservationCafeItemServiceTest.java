package com.tys.service;

import com.tys.dto.ReservationCafeItemDto;
import com.tys.mapper.ReservationCafeItemMapper;
import com.tys.model.Cafe;
import com.tys.model.Company;
import com.tys.model.Reservation;
import com.tys.model.ReservationCafeItem;
import com.tys.repository.CafeRepository;
import com.tys.repository.ReservationCafeItemRepository;
import com.tys.repository.ReservationRepository;
import com.tys.request.UpdateReservationCafeItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Kafe hesabı işletme izolasyonu (P0-1 regresyonu). */
class ReservationCafeItemServiceTest {

    private static final long COMPANY_A = 1L;
    private static final long COMPANY_B = 2L;

    private ReservationRepository reservationRepository;
    private CafeRepository cafeRepository;
    private ReservationCafeItemRepository itemRepository;
    private ReservationCafeItemService service;
    private Reservation reservationOfA;

    @BeforeEach
    void setUp() {
        reservationRepository = mock(ReservationRepository.class);
        cafeRepository = mock(CafeRepository.class);
        itemRepository = mock(ReservationCafeItemRepository.class);
        ReservationCafeItemMapper mapper = mock(ReservationCafeItemMapper.class);
        service = new ReservationCafeItemService(reservationRepository, cafeRepository, itemRepository, mapper);

        Company a = new Company();
        a.setId(COMPANY_A);
        reservationOfA = new Reservation();
        reservationOfA.setId(10L);
        reservationOfA.setCompany(a);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservationOfA));
        when(itemRepository.findAllByReservation_Id(10L)).thenReturn(List.of());
    }

    @Test
    void baskaIsletmeRezervasyonHesabiniOkuyamaz() {
        assertForbidden(() -> service.getReservationCafeItemDetails(10L, COMPANY_B));
        assertForbidden(() -> service.getReservationCafeAmount(10L, COMPANY_B));
        assertForbidden(() -> service.getReservationCafeItemList(10L, COMPANY_B));
    }

    @Test
    void sahipIsletmeHesabiOkuyabilir() {
        assertThat(service.getReservationCafeAmount(10L, COMPANY_A)).isZero();
    }

    @Test
    void baskaIsletmeRezervasyonaKalemEkleyemez() {
        ReservationCafeItemDto dto = new ReservationCafeItemDto();
        dto.setReservationId(10L);
        dto.setCafeId(5L);
        dto.setCount(1);
        assertForbidden(() -> service.addCafeItemToReservation(dto, COMPANY_B));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void baskaIsletmeninUrunuRezervasyonaEklenemez() {
        Cafe cafeOfB = new Cafe();
        cafeOfB.setId(5L);
        cafeOfB.setCompanyId(COMPANY_B);
        when(cafeRepository.findById(5L)).thenReturn(Optional.of(cafeOfB));
        ReservationCafeItemDto dto = new ReservationCafeItemDto();
        dto.setReservationId(10L);
        dto.setCafeId(5L);
        dto.setCount(1);
        assertForbidden(() -> service.addCafeItemToReservation(dto, COMPANY_A));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void topluGuncellemeBaskaRezervasyonunKaleminiDegistiremez() {
        Company b = new Company();
        b.setId(COMPANY_B);
        Reservation reservationOfB = new Reservation();
        reservationOfB.setId(20L);
        reservationOfB.setCompany(b);
        ReservationCafeItem itemOfB = new ReservationCafeItem();
        itemOfB.setId(99L);
        itemOfB.setReservation(reservationOfB);
        when(itemRepository.findById(99L)).thenReturn(Optional.of(itemOfB));

        UpdateReservationCafeItemRequest request = new UpdateReservationCafeItemRequest();
        request.setId(99L);
        request.setCount(0);
        // A kendi rezervasyonunun yolundan B'nin kalemini silmeye çalışır
        assertForbidden(() -> service.updateCafeItemCounts(10L, List.of(request), COMPANY_A));
        verify(itemRepository, never()).delete(any());
    }

    private static void assertForbidden(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }
}
