package com.tys.service;

import com.tys.model.Guest;
import com.tys.model.KbsNotice;
import com.tys.repository.GuestRepository;
import com.tys.repository.KbsNoticeRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KbsNotificationService {

    static final String QUEUED = "Konaklama kaydı tamamlandı. KBS bildirimi arka planda gönderiliyor.";

    private final GuestRepository guestRepository;
    private final KbsNoticeRepository kbsNoticeRepository;
    private final ReservationService reservationService;

    public KbsNotificationService(
            GuestRepository guestRepository,
            KbsNoticeRepository kbsNoticeRepository,
            @Lazy ReservationService reservationService) {
        this.guestRepository = guestRepository;
        this.kbsNoticeRepository = kbsNoticeRepository;
        this.reservationService = reservationService;
    }

    @Async
    public void dispatch(Long reservationId, List<Long> guestIds, boolean checkIn) {
        if (reservationId == null || guestIds == null || guestIds.isEmpty()) {
            return;
        }
        String warning;
        try {
            List<Guest> guests = guestRepository.findAllById(guestIds);
            warning = reservationService.notifyGuestsForKbs(guests, checkIn);
        } catch (Exception ex) {
            warning = ex.getMessage() != null ? ex.getMessage() : "KBS bildirimi yapılamadı.";
        }
        if (warning == null || warning.isBlank()) {
            return;
        }
        saveNotice(reservationId, warning);
    }

    @Transactional
    public void saveNotice(Long reservationId, String warning) {
        KbsNotice notice = new KbsNotice();
        notice.setReservationId(reservationId);
        notice.setMessage(warning.length() > 2000 ? warning.substring(0, 2000) : warning);
        notice.setCreatedAt(LocalDateTime.now());
        kbsNoticeRepository.save(notice);
    }
}
