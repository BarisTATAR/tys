package com.tys.service;

import com.tys.dto.RulesApprovalPageDto;
import com.tys.dto.RulesFileDto;
import com.tys.dto.SendRulesSmsResponse;
import com.tys.model.Guest;
import com.tys.model.Reservation;
import com.tys.model.RulesApprovalLink;
import com.tys.repository.CompanyRepository;
import com.tys.repository.ReservationRepository;
import com.tys.repository.RulesApprovalLinkRepository;
import com.tys.request.CreateSMSRequest;
import com.tys.request.SendRulesSmsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RulesSmsService {

    private final RulesFileService rulesFileService;
    private final CompanyRepository companyRepository;
    private final ReservationRepository reservationRepository;
    private final RulesApprovalLinkRepository rulesApprovalLinkRepository;
    private final VatanSmsService vatanSmsService;

    @Value("${app.frontend-base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Transactional
    public SendRulesSmsResponse sendRulesSms(Long companyId, SendRulesSmsRequest request) {
        if (request.getReservationId() == null) {
            throw new IllegalArgumentException("Rezervasyon seçimi zorunludur.");
        }

        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Rezervasyon bulunamadı."));

        if (reservation.getCompany() == null || !companyId.equals(reservation.getCompany().getId())) {
            throw new IllegalArgumentException("Bu rezervasyon işletmenize ait değil.");
        }

        // Lazy guests yükle
        if (reservation.getGuests() != null) {
            reservation.getGuests().size();
        }

        Guest contactGuest = findContactGuest(reservation.getGuests());

        String phone = normalizePhone(
                firstNonBlank(request.getPhoneNumber(),
                        contactGuest != null ? contactGuest.getPhoneNumber() : null));
        if (phone == null) {
            throw new IllegalArgumentException(
                    "Telefon numarası giriniz veya rezervasyonda iletişim misafiri telefonu tanımlayın.");
        }

        String guestName = firstNonBlank(
                request.getGuestName(),
                formatGuestName(contactGuest));

        rulesFileService.getByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Önce işletme kuralları dosyasını yüklemelisiniz."));

        String companyName = companyRepository.findById(companyId)
                .map(c -> c.getName() != null ? c.getName() : "İşletmemiz")
                .orElse("İşletmemiz");

        RulesApprovalLink link = new RulesApprovalLink();
        link.setToken(UUID.randomUUID().toString().replace("-", ""));
        link.setCompanyId(companyId);
        link.setReservationId(reservation.getId());
        link.setPhoneNumber(phone);
        link.setGuestName(guestName);
        link.setApproved(false);
        link.setCreatedAt(LocalDateTime.now());
        rulesApprovalLinkRepository.save(link);

        String approvalUrl = buildApprovalUrl(link.getToken());

        String message = request.getMessage();
        if (message == null || message.isBlank()) {
            message = buildDefaultMessage(companyName, guestName, approvalUrl);
        } else if (!message.contains(approvalUrl)) {
            String trimmed = message.trim();
            if (!trimmed.toLowerCase().contains("özel link") && !trimmed.toLowerCase().contains("ozel link")) {
                message = trimmed + " Kuralları görmek ve onaylamak için size özel link: " + approvalUrl;
            } else {
                message = trimmed + " " + approvalUrl;
            }
        }

        CreateSMSRequest smsRequest = CreateSMSRequest.builder()
                .phoneList(List.of(phone))
                .senderName(companyName)
                .message(message.trim())
                .build();

        vatanSmsService.sendSms(smsRequest);

        return SendRulesSmsResponse.builder()
                .message("İşletme kuralları onay linki SMS ile gönderildi.")
                .approvalUrl(approvalUrl)
                .token(link.getToken())
                .reservationId(reservation.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public RulesApprovalPageDto getApprovalPage(String token) {
        RulesApprovalLink link = rulesApprovalLinkRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz veya süresi dolmuş onay linki."));

        RulesFileDto rules = rulesFileService.getByCompanyId(link.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("İşletme kuralları bulunamadı."));

        String companyName = companyRepository.findById(link.getCompanyId())
                .map(c -> c.getName() != null ? c.getName() : "İşletme")
                .orElse("İşletme");

        return RulesApprovalPageDto.builder()
                .reservationId(link.getReservationId())
                .companyName(companyName)
                .guestName(link.getGuestName())
                .rulesText(rules.getRulesText())
                .approved(link.isApproved())
                .approvedAt(link.getApprovedAt())
                .build();
    }

    @Transactional
    public RulesApprovalPageDto approve(String token) {
        RulesApprovalLink link = rulesApprovalLinkRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz veya süresi dolmuş onay linki."));

        if (!link.isApproved()) {
            link.setApproved(true);
            link.setApprovedAt(LocalDateTime.now());
            rulesApprovalLinkRepository.save(link);
        }

        return getApprovalPage(token);
    }

    @Transactional(readOnly = true)
    public List<RulesApprovalLink> getApprovalsByReservation(Long companyId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Rezervasyon bulunamadı."));
        if (reservation.getCompany() == null || !companyId.equals(reservation.getCompany().getId())) {
            throw new IllegalArgumentException("Bu rezervasyon işletmenize ait değil.");
        }
        return rulesApprovalLinkRepository.findAllByReservationIdOrderByCreatedAtDesc(reservationId);
    }

    public String buildDefaultMessage(String companyName, String guestName, String approvalUrl) {
        String greeting = (guestName != null && !guestName.isBlank())
                ? "Sayın " + guestName.trim() + ","
                : "Sayın misafirimiz,";

        return greeting + " "
                + companyName + " işletme kurallarını okumanızı ve onaylamanızı rica ederiz. "
                + "Kuralları görmek ve onaylamak için size özel link: " + approvalUrl;
    }

    private String buildApprovalUrl(String token) {
        String base = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        return base + "/kurallar-onay/" + token;
    }

    private static Guest findContactGuest(List<Guest> guests) {
        if (guests == null || guests.isEmpty()) {
            return null;
        }
        return guests.stream()
                .filter(g -> Boolean.TRUE.equals(g.getIsContact()))
                .findFirst()
                .orElse(guests.get(0));
    }

    private static String formatGuestName(Guest guest) {
        if (guest == null) {
            return null;
        }
        String name = ((guest.getName() != null ? guest.getName() : "") + " "
                + (guest.getSurname() != null ? guest.getSurname() : "")).trim();
        return name.isBlank() ? null : name;
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return null;
    }

    private static String normalizePhone(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith("90") && digits.length() >= 12) {
            return digits;
        }
        if (digits.startsWith("0") && digits.length() == 11) {
            return "90" + digits.substring(1);
        }
        if (digits.length() == 10) {
            return "90" + digits;
        }
        return digits.length() >= 10 ? digits : null;
    }
}
