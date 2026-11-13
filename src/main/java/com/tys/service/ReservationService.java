package com.tys.service;

import com.tys.client.MusteriKimlikNoCikisTalep;
import com.tys.client.MusteriKimlikNoGirisTalep;
import com.tys.client.ObjectFactory;
import com.tys.client.SnfEnumKonaklayanCikisTurleri;
import com.tys.client.SnfEnumKonaklayanKullanimSekli;
import com.tys.client.Sonuc;
import com.tys.dto.ReservationDto;
import com.tys.dto.RoomDto;
import com.tys.enums.ReservationStatus;
import com.tys.mapper.GuestMapper;
import com.tys.mapper.ReservationMapper;
import com.tys.model.Company;
import com.tys.model.Payment;
import com.tys.model.Reservation;
import com.tys.model.Room;
import com.tys.model.Guest;
import com.tys.repository.CompanyRepository;
import com.tys.repository.GuestRepository;
import com.tys.repository.KbsNoticeRepository;
import com.tys.repository.PaymentRepository;
import com.tys.repository.ReservationRepository;
import com.tys.repository.RoomRepository;
import com.tys.request.*;
import com.tys.response.EarlyCheckOutOutcome;
import com.tys.response.KbsGuestCheckInResponse;
import com.tys.response.KbsGuestCheckOutResponse;
import com.tys.util.ReservationDateUtils;
import com.tys.validation.InputRules;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final GuestMapper guestMapper;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final PaymentRepository paymentRepository;
    private final CompanyRepository companyRepository;
    private final ReservationCafeItemService reservationCafeItemService;
    private final RoomService roomService;
    private final VatanSmsService vatanSmsService;
    private final KbsService kbsService;
    private final KbsNoticeRepository kbsNoticeRepository;
    private final ObjectProvider<KbsNotificationService> kbsNotifications;
    private final String RESERVATION_MADE_MESSAGE = " rezervasyonunuz başarıyla oluşturulmuştur.";
    private final String CHECK_IN_DONE_MESSAGE = " giriş işlemleriniz başarıyla tamamlanmıştır.";
    private final String CHECK_OUT_DONE_MESSAGE = " çıkış işlemleriniz başarıyla tamamlanmıştır. Görüşleriniz bizim için değerli, bizi değerlendirmeyi unutmayın.";

    public ReservationService(ReservationRepository reservationRepository, ReservationMapper reservationMapper, GuestMapper guestMapper, RoomRepository roomRepository, GuestRepository guestRepository, PaymentRepository paymentRepository, CompanyRepository companyRepository, ReservationCafeItemService reservationCafeItemService, RoomService roomService, VatanSmsService vatanSmsService, KbsService kbsService, KbsNoticeRepository kbsNoticeRepository, ObjectProvider<KbsNotificationService> kbsNotifications) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.guestMapper = guestMapper;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.paymentRepository = paymentRepository;
        this.companyRepository = companyRepository;
        this.reservationCafeItemService = reservationCafeItemService;
        this.roomService = roomService;
        this.vatanSmsService = vatanSmsService;
        this.kbsService = kbsService;
        this.kbsNoticeRepository = kbsNoticeRepository;
        this.kbsNotifications = kbsNotifications;
    }

    @Transactional
    public String updateReservation(UpdateReservationRequest request, Long companyId) throws Exception {
        Reservation existingReservation = reservationRepository.findById(request.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervasyon bulunamadı."));
        assertReservationCompany(existingReservation, companyId);
        validateUpdateInput(request);
        boolean checkingOut = request.getReservationStatus() == ReservationStatus.CHECK_OUT_DONE
                && existingReservation.getReservationStatus() != ReservationStatus.CHECK_OUT_DONE;
        boolean checkingIn = request.getReservationStatus() == ReservationStatus.CHECK_IN_DONE
                && existingReservation.getReservationStatus() != ReservationStatus.CHECK_IN_DONE;
        // 1️⃣ Eğer status CHECKOUT yapılmak isteniyorsa borç kontrolü yap

        Guest contactGuest = new Guest();
        if (request.getGuests() != null) {
            for (Guest guest : request.getGuests()) {
                if (Boolean.TRUE.equals(guest.getIsContact())) {
                    contactGuest.setName(guest.getName());
                    contactGuest.setSurname(guest.getSurname());
                    contactGuest.setPhoneNumber(guest.getPhoneNumber());
                }
            }
        }
        if (request.getReservationStatus() == ReservationStatus.RESERVATION_MADE) {
//            vatanSmsService.sendSms(new CreateSMSRequest(List.of(request.getGuests().get(0).getPhoneNumber()), "",
//                    contactGuest.getName() + " " + contactGuest.getSurname() + RESERVATION_MADE_MESSAGE));
        } else if (checkingIn) {
            assertCheckInHasAllGuests(request, existingReservation);
//            vatanSmsService.sendSms(new CreateSMSRequest(List.of(request.getGuests().get(0).getPhoneNumber()), "",
//                    contactGuest.getName() + " " + contactGuest.getSurname() + CHECK_IN_DONE_MESSAGE));
        } else if (checkingOut) {
            isAllPaymentsCompleted(request, existingReservation);
//            vatanSmsService.sendSms(new CreateSMSRequest(List.of(request.getGuests().get(0).getPhoneNumber()), "",
//                    contactGuest.getName() + " " + contactGuest.getSurname() + CHECK_OUT_DONE_MESSAGE + " " +
//                            existingReservation.getCompany().getGoogleCommentsUrl()));
        }

        reservationMapper.updateExistingReservationWithReservationRequest(request, existingReservation);
        if (request.getCheckInDate() != null) {
            existingReservation.setCheckInDate(ReservationDateUtils.toStartOfDay(request.getCheckInDate()));
        }
        if (request.getCheckOutDate() != null) {
            existingReservation.setCheckOutDate(ReservationDateUtils.toStartOfDay(request.getCheckOutDate()));
        }
        if (request.getGuests() != null) {
            if (existingReservation.getGuests() != null) {
                existingReservation.getGuests().size();
            }
            syncReservationGuests(existingReservation, request.getGuests());
        }
        List<Guest> kbsCheckOutTargets = new ArrayList<>();
        if (checkingOut && existingReservation.getGuests() != null) {
            LocalDateTime checkedOutAt = LocalDateTime.now();
            for (Guest guest : existingReservation.getGuests()) {
                if (isAddedGuest(guest) && guest.getCheckOutDate() == null) {
                    kbsCheckOutTargets.add(guest);
                    guest.setCheckOutDate(checkedOutAt);
                }
            }
        }
        List<Guest> kbsCheckInTargets = new ArrayList<>();
        if (checkingIn) {
            LocalDateTime checkedInAt = LocalDateTime.now();
            List<Guest> source = request.getGuests() != null ? request.getGuests() : existingReservation.getGuests();
            if (source != null) {
                for (Guest guest : source) {
                    if (isAddedGuest(guest)) {
                        kbsCheckInTargets.add(guest);
                    }
                }
            }
            if (existingReservation.getGuests() != null) {
                for (Guest guest : existingReservation.getGuests()) {
                    if (isAddedGuest(guest) && guest.getCheckInDate() == null) {
                        guest.setCheckInDate(checkedInAt);
                    }
                }
            }
        }
        if (request.getRoomList() != null) {
            if (existingReservation.getCompany() != null) {
                existingReservation.getCompany().getId();
            }
            if (existingReservation.getRooms() != null) {
                existingReservation.getRooms().size();
            }
            syncReservationRooms(existingReservation, request.getRoomList());
        }
        InputRules.stayCounts(
                request.getAdultGuestNumber() != null ? request.getAdultGuestNumber() : existingReservation.getAdultGuestNumber(),
                request.getChildGuestNumber() != null ? request.getChildGuestNumber() : existingReservation.getChildGuestNumber(),
                request.getBabyGuestNumber() != null ? request.getBabyGuestNumber() : existingReservation.getBabyGuestNumber(),
                totalRoomCapacity(existingReservation));
        reservationRepository.save(existingReservation);
        if (checkingIn) {
            queueKbs(existingReservation.getId(), kbsCheckInTargets, existingReservation, true);
            return KbsNotificationService.QUEUED;
        }
        if (checkingOut) {
            queueKbs(existingReservation.getId(), kbsCheckOutTargets, existingReservation, false);
            return KbsNotificationService.QUEUED;
        }
        return null;
    }

    private void assertReservationCompany(Reservation reservation, Long companyId) {
        if (companyId == null
                || reservation.getCompany() == null
                || !companyId.equals(reservation.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
        }
    }

    /**
     * Check-in için adı ve soyadı girilmiş misafir sayısı, yetişkin ve çocuk toplamına eşit olmalıdır.
     */
    private void validateUpdateInput(UpdateReservationRequest request) {
        if (request.getPaymentList() != null) {
            for (Payment payment : request.getPaymentList()) {
                if (payment != null) {
                    InputRules.payment(payment.getAmount(), payment.getAdvancePayment());
                }
            }
        }
        if (request.getGuests() == null) {
            return;
        }
        for (Guest guest : request.getGuests()) {
            if (guest == null) {
                continue;
            }
            String previousIdentity = null;
            String previousPhone = null;
            if (guest.getId() != null) {
                Guest stored = guestRepository.findById(guest.getId()).orElse(null);
                if (stored != null) {
                    previousIdentity = stored.getIdentityNumber();
                    previousPhone = stored.getPhoneNumber();
                }
            }
            InputRules.guestFields(
                    guest.getName(),
                    guest.getSurname(),
                    guest.getIdentityNumber(),
                    guest.getPhoneNumber(),
                    guest.getEmail(),
                    guest.getPlateNumber(),
                    previousIdentity,
                    previousPhone);
        }
    }

    private int totalRoomCapacity(Reservation reservation) {
        if (reservation.getRooms() == null || reservation.getRooms().isEmpty()) {
            return 0;
        }
        int sum = 0;
        boolean known = false;
        for (Room room : reservation.getRooms()) {
            if (room != null && room.getCapacity() != null) {
                sum += room.getCapacity();
                known = true;
            }
        }
        return known ? sum : 0;
    }

    private void assertCheckInHasAllGuests(UpdateReservationRequest request, Reservation existingReservation) {
        int adults = request.getAdultGuestNumber() != null
                ? request.getAdultGuestNumber()
                : (existingReservation.getAdultGuestNumber() != null ? existingReservation.getAdultGuestNumber() : 0);
        int children = request.getChildGuestNumber() != null
                ? request.getChildGuestNumber()
                : (existingReservation.getChildGuestNumber() != null ? existingReservation.getChildGuestNumber() : 0);
        int required = adults + children;

        List<Guest> guests = request.getGuests() != null ? request.getGuests() : existingReservation.getGuests();
        int added = 0;
        if (guests != null) {
            for (Guest guest : guests) {
                if (isAddedGuest(guest)) {
                    added++;
                }
            }
        }
        if (added != required) {
            throw new RuntimeException(
                    "Konaklayacak yetişkin ve çocukların adı ve soyadı girilmeden check-in yapılamaz.");
        }
    }

    private boolean isAddedGuest(Guest guest) {
        if (guest == null) {
            return false;
        }
        return hasText(guest.getName()) && hasText(guest.getSurname());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Request'ten gelen Guest'ler detached'tır; cascade PERSIST hata verir.
     * Mevcut misafirleri managed entity üzerinden günceller / yenileri kaydeder.
     */
    private void syncReservationGuests(Reservation reservation, List<Guest> requestGuests) {
        List<Guest> managedGuests = new ArrayList<>();
        for (Guest incoming : requestGuests) {
            if (incoming == null) {
                continue;
            }
            Guest managed;
            if (incoming.getId() != null) {
                managed = guestRepository.findById(incoming.getId())
                        .orElseThrow(() -> new RuntimeException("Misafir bulunamadı. Id: " + incoming.getId()));
                if (incoming.getName() != null) {
                    managed.setName(incoming.getName());
                }
                if (incoming.getSurname() != null) {
                    managed.setSurname(incoming.getSurname());
                }
                if (incoming.getIdentityNumber() != null) {
                    managed.setIdentityNumber(incoming.getIdentityNumber());
                }
                if (incoming.getPhoneNumber() != null) {
                    managed.setPhoneNumber(incoming.getPhoneNumber());
                }
                if (incoming.getEmail() != null) {
                    managed.setEmail(incoming.getEmail());
                }
                if (incoming.getPlateNumber() != null) {
                    managed.setPlateNumber(incoming.getPlateNumber());
                }
                if (incoming.getIsContact() != null) {
                    managed.setIsContact(incoming.getIsContact());
                }
                if (incoming.getJob() != null) {
                    managed.setJob(incoming.getJob());
                }
                if (incoming.getAddress() != null) {
                    managed.setAddress(incoming.getAddress());
                }
            } else {
                managed = guestRepository.save(incoming);
            }
            managedGuests.add(managed);
        }

        if (reservation.getGuests() == null) {
            reservation.setGuests(new ArrayList<>());
        } else {
            reservation.getGuests().clear();
        }
        reservation.getGuests().addAll(managedGuests);
    }

    private void syncReservationRooms(Reservation reservation, List<CreateRoomRequest> roomRequests) {
        if (roomRequests == null || roomRequests.isEmpty()) {
            throw new RuntimeException("Rezervasyonda en az bir oda olmalıdır.");
        }
        if (reservation.getCompany() == null || reservation.getCompany().getId() == null) {
            throw new RuntimeException("Rezervasyonun işletme bilgisi bulunamadı.");
        }

        Long companyId = reservation.getCompany().getId();
        LocalDate reqIn = ReservationDateUtils.toLocalDate(reservation.getCheckInDate());
        LocalDate reqOut = ReservationDateUtils.toLocalDate(reservation.getCheckOutDate());
        // Aynı gün giriş-çıkış (günlük kullanım) serbest; yalnızca çıkış < giriş engellenir
        if (reqIn == null || reqOut == null || reqOut.isBefore(reqIn)) {
            throw new RuntimeException("Geçerli giriş/çıkış tarihleri gereklidir.");
        }

        List<Room> currentRooms = reservation.getRooms() != null
                ? new ArrayList<>(reservation.getRooms())
                : new ArrayList<>();
        List<Long> currentRoomIds = currentRooms.stream().map(Room::getId).toList();

        // Müsait odaları gün bazlı kontrol ile al (mevcut rezervasyon hariç)
        List<Long> availableIds = roomService
                .getAvailableRoomsExcludingReservation(
                        reqIn.atStartOfDay(),
                        reqOut.atStartOfDay(),
                        companyId,
                        reservation.getId())
                .stream()
                .map(RoomDto::getId)
                .toList();

        List<Room> newRooms = new ArrayList<>();
        for (CreateRoomRequest roomFromRequest : roomRequests) {
            if (roomFromRequest.getNumber() == null) {
                throw new RuntimeException("Oda numarası boş olamaz.");
            }
            Room room = roomRepository.findByNumberAndCompanyId(roomFromRequest.getNumber(), companyId)
                    .orElseThrow(() -> new RuntimeException(
                            "Oda bulunamadı. Oda no: " + roomFromRequest.getNumber() + ", işletme id: " + companyId));

            boolean alreadyOnReservation = currentRoomIds.contains(room.getId());
            boolean available = availableIds.contains(room.getId());
            if (!alreadyOnReservation && !available) {
                throw new RuntimeException(
                        "Oda " + room.getNumber() + " seçilen tarihlerde müsait değil (başka rezervasyon var).");
            }
            if (newRooms.stream().noneMatch(r -> r.getId().equals(room.getId()))) {
                newRooms.add(room);
            }
        }

        if (newRooms.isEmpty()) {
            throw new RuntimeException("Rezervasyonda en az bir oda olmalıdır.");
        }

        if (reservation.getRooms() == null) {
            reservation.setRooms(new ArrayList<>());
        } else {
            reservation.getRooms().clear();
        }
        reservation.getRooms().addAll(newRooms);
    }

    /**
     * KBS reddi otel kaydını geri almaz. Bildirilemeyen kişiler mesaj olarak döner.
     */
    public String notifyGuestsForKbs(List<Guest> guests, boolean checkIn) {
        return notifyKbs(guests, checkIn);
    }

    private void queueKbs(Long reservationId, List<Guest> targets, Reservation reservation, boolean checkIn) {
        List<Long> ids = new ArrayList<>();
        if (targets != null) {
            for (Guest target : targets) {
                if (target == null) {
                    continue;
                }
                if (target.getId() != null) {
                    ids.add(target.getId());
                    continue;
                }
                if (reservation.getGuests() == null) {
                    continue;
                }
                for (Guest managed : reservation.getGuests()) {
                    if (managed.getId() != null && samePerson(managed, target)) {
                        ids.add(managed.getId());
                    }
                }
            }
        }
        if (ids.isEmpty()) {
            return;
        }
        List<Long> frozen = List.copyOf(ids);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kbsNotifications.getObject().dispatch(reservationId, frozen, checkIn);
            }
        });
    }

    private boolean samePerson(Guest left, Guest right) {
        String leftName = left.getName() == null ? "" : left.getName().trim();
        String rightName = right.getName() == null ? "" : right.getName().trim();
        String leftSurname = left.getSurname() == null ? "" : left.getSurname().trim();
        String rightSurname = right.getSurname() == null ? "" : right.getSurname().trim();
        return leftName.equalsIgnoreCase(rightName) && leftSurname.equalsIgnoreCase(rightSurname);
    }

    private String notifyKbs(List<Guest> guests, boolean checkIn) {
        if (guests == null || guests.isEmpty()) {
            return null;
        }
        StringBuilder warnings = new StringBuilder();
        for (Guest guest : guests) {
            try {
                if (checkIn) {
                    checkInGuestOnKbs(guest);
                } else {
                    checkOutGuestOnKbs(guest);
                }
            } catch (Exception ex) {
                if (warnings.length() > 0) {
                    warnings.append(' ');
                }
                warnings.append(ex.getMessage() != null ? ex.getMessage() : "KBS bildirimi yapılamadı.");
            }
        }
        return warnings.length() == 0 ? null : warnings.toString();
    }

    /**
     * Yalnızca seçilen misafir çıkar. Kalan misafirler varken rezervasyon tarihi ve durumu değişmez.
     * Konaklayan kimse kalmazsa rezervasyonun kendisi de check-out olur.
     *
     * @return rezervasyonun tamamı kapandıysa true
     */
    @Transactional
    public EarlyCheckOutOutcome earlyCheckOutGuest(Long reservationId, Long guestId, EarlyGuestCheckOutRequest request, Long companyId) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervasyon bulunamadı."));
        assertReservationCompany(reservation, companyId);
        if (reservation.getReservationStatus() != ReservationStatus.CHECK_IN_DONE) {
            throw new RuntimeException("Erken check-out için önce check-in yapılmış olmalıdır.");
        }
        if (reservation.getGuests() != null) {
            reservation.getGuests().size();
        }

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Misafir bulunamadı."));
        boolean onReservation = reservation.getGuests() != null
                && reservation.getGuests().stream().anyMatch(existing -> guestId.equals(existing.getId()));
        if (!onReservation) {
            throw new RuntimeException("Bu misafir rezervasyona bağlı değil.");
        }
        if (guest.getCheckOutDate() != null) {
            throw new RuntimeException("Bu misafir zaten çıkış yapmış.");
        }

        if (request != null) {
            if (hasText(request.getName())) {
                guest.setName(request.getName().trim());
            }
            if (hasText(request.getSurname())) {
                guest.setSurname(request.getSurname().trim());
            }
            if (hasText(request.getIdentityNumber())) {
                guest.setIdentityNumber(request.getIdentityNumber().trim());
            }
        }
        if (!isAddedGuest(guest)) {
            throw new RuntimeException("Erken check-out için misafirin adı ve soyadı girilmiş olmalıdır.");
        }

        LocalDate today = LocalDate.now();
        LocalDate checkIn = ReservationDateUtils.toLocalDate(reservation.getCheckInDate());
        if (checkIn != null && today.isBefore(checkIn)) {
            throw new RuntimeException("Erken check-out için çıkış tarihi giriş tarihinden önce olamaz.");
        }

        boolean lastGuest = reservation.getGuests().stream()
                .filter(this::isAddedGuest)
                .filter(other -> !guestId.equals(other.getId()))
                .allMatch(other -> other.getCheckOutDate() != null);
        if (lastGuest) {
            UpdateReservationRequest paymentRequest = new UpdateReservationRequest();
            paymentRequest.setId(reservation.getId());
            isAllPaymentsCompleted(paymentRequest, reservation);
        }

        guest.setCheckOutDate(LocalDateTime.now());

        if (lastGuest) {
            reservation.setCheckOutDate(today.atStartOfDay());
            reservation.setReservationStatus(ReservationStatus.CHECK_OUT_DONE);
        }
        reservationRepository.save(reservation);
        queueKbs(reservation.getId(), List.of(guest), reservation, false);
        return new EarlyCheckOutOutcome(lastGuest, KbsNotificationService.QUEUED);
    }

    private void checkInGuestOnKbs(Guest guest) throws Exception {
        String identity = guest.getIdentityNumber() == null ? "" : guest.getIdentityNumber().replaceAll("\\D", "");
        String guestName = guest.getName().trim() + " " + guest.getSurname().trim();
        if (identity.isEmpty()) {
            throw new RuntimeException(guestName + " için T.C. kimlik numarası olmadan KBS girişi yapılamadı.");
        }
        try {
            Long.parseLong(identity);
        } catch (NumberFormatException ex) {
            throw new RuntimeException(guestName + " için T.C. kimlik numarası geçersiz.");
        }
        KbsGuestCheckInResponse response = kbsService.checkInGuest(getGuestForKBS(guest));
        if (response == null || response.getMusteri() == null) {
            throw new RuntimeException(guestName + " için KBS girişi yapılamadı.");
        }
    }

    private void checkOutGuestOnKbs(Guest guest) throws Exception {
        String identity = guest.getIdentityNumber() == null ? "" : guest.getIdentityNumber().replaceAll("\\D", "");
        String guestName = guest.getName().trim() + " " + guest.getSurname().trim();
        if (identity.isEmpty()) {
            throw new RuntimeException(guestName + " için T.C. kimlik numarası olmadan KBS çıkışı yapılamaz.");
        }
        long kimlikNo;
        try {
            kimlikNo = Long.parseLong(identity);
        } catch (NumberFormatException ex) {
            throw new RuntimeException(guestName + " için T.C. kimlik numarası geçersiz.");
        }

        ObjectFactory factory = new ObjectFactory();
        MusteriKimlikNoCikisTalep talep = factory.createMusteriKimlikNoCikisTalep();
        talep.setCKSTIP(SnfEnumKonaklayanCikisTurleri.TESISTENCIKIS);
        talep.setCKSTRH(factory.createMusteriKimlikNoCikisTalepCKSTRH(convert(LocalDateTime.now())));
        talep.setKIMLIKNO(factory.createMusteriKimlikNoCikisTalepKIMLIKNO(kimlikNo));

        KbsGuestCheckOutRequest kbsRequest = new KbsGuestCheckOutRequest();
        kbsRequest.setMusteri(talep);
        KbsGuestCheckOutResponse response = kbsService.checkOutGuest(kbsRequest);
        Sonuc sonuc = response != null ? response.getSonuc() : null;
        if (sonuc == null || !Boolean.TRUE.equals(sonuc.isBasarili())) {
            throw new RuntimeException(guestName + " için KBS çıkışı yapılamadı. " + kbsResultMessage(sonuc));
        }
    }

    private String kbsResultMessage(Sonuc sonuc) {
        if (sonuc == null) {
            return "KBS yanıt vermedi.";
        }
        if (sonuc.getMesaj() != null && sonuc.getMesaj().getValue() != null && !sonuc.getMesaj().getValue().isBlank()) {
            return sonuc.getMesaj().getValue();
        }
        if (sonuc.getHataKodu() != null && !sonuc.getHataKodu().isEmpty()) {
            return String.join(", ", sonuc.getHataKodu());
        }
        return "KBS çıkışı başarısız.";
    }

    private void isAllPaymentsCompleted(UpdateReservationRequest request, Reservation existingReservation) {
        // Rezervasyona bağlı tüm ödemeleri al
        List<Payment> payments = paymentRepository.findAllByReservationId(existingReservation.getId());
        if (payments == null || payments.isEmpty()) {
            throw new RuntimeException("Ödeme kaydı olmadan check-out yapılamaz.");
        }

        // Toplam ödenen tutarı hesapla
        BigDecimal totalPaid = payments.get(0).getAdvancePayment() != null
                ? payments.get(0).getAdvancePayment()
                : BigDecimal.ZERO;
        for (Payment p : payments) {
            if (p.getPaidAmount() != null) {
                totalPaid = totalPaid.add(p.getPaidAmount());
            }
        }

        // İlk ödemenin tutarı boş veya 0 ise borç 0 kabul edilir.
        BigDecimal reservationAmount = payments.get(0).getAmount() != null
                ? payments.get(0).getAmount()
                : BigDecimal.ZERO;
        BigDecimal cafeAmount = reservationCafeItemService.getReservationCafeAmountByReservationId(
                existingReservation.getId());
        if (cafeAmount == null) {
            cafeAmount = BigDecimal.ZERO;
        }
        BigDecimal totalAmount = reservationAmount.add(cafeAmount);

        // Borç devam ediyorsa checkout’a izin verme
        if (totalPaid.compareTo(totalAmount) < 0) {
            throw new RuntimeException(
                    "Tüm borçlar kapatılmadan Check-out yapılamaz. " +
                            "Ödenen Tutar: " + totalPaid + ", Ödenecek Tutar: " + (totalAmount.subtract(totalPaid))
            );
        }
    }

    @Transactional
    public void createReservation(CreateReservationRequest request, Long companyId) {
        if (request.getRoomList() == null || request.getRoomList().isEmpty()) {
            throw new RuntimeException("Rezervasyonda en az bir oda olmalıdır.");
        }
        InputRules.stayCounts(
                request.getAdultGuestNumber(),
                request.getChildGuestNumber(),
                request.getBabyGuestNumber(),
                0);
        InputRules.plainText(request.getReservationMessage(), "Açıklama", InputRules.MAX_TEXT, false);
        if (request.getGuestList() != null) {
            for (CreateGuestRequest guest : request.getGuestList()) {
                if (guest == null) {
                    continue;
                }
                InputRules.guestFields(
                        guest.getName(),
                        guest.getSurname(),
                        guest.getIdentityNumber(),
                        guest.getPhoneNumber(),
                        guest.getEmail(),
                        guest.getPlateNumber(),
                        null,
                        null);
            }
        }
        if (request.getPaymentList() != null) {
            for (CreatePaymentRequest payment : request.getPaymentList()) {
                if (payment != null) {
                    InputRules.payment(payment.getAmount(), payment.getAdvancePayment());
                }
            }
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        Reservation reservation = reservationMapper.createReservationRequestToEntity(request);
        reservation.setCompany(company);
        // Mapper oda listesini doldurmuş olabilir; müsaitlik/tarih kuralları sync içinde uygulanır
        if (reservation.getRooms() != null) {
            reservation.getRooms().clear();
        }

        // P0: tarih (checkOut > checkIn) + oda çakışması + min 1 oda
        syncReservationRooms(reservation, request.getRoomList());
        InputRules.stayCounts(
                request.getAdultGuestNumber(),
                request.getChildGuestNumber(),
                request.getBabyGuestNumber(),
                totalRoomCapacity(reservation));

        if (request.getGuestList() != null && !request.getGuestList().isEmpty()) {
            reservation.setGuests(guestMapper.toEntityList(request.getGuestList()));
        } else {
            reservation.setGuests(new ArrayList<>());
        }

        reservationRepository.save(reservation);
    }

    public void deleteReservation(DeleteReservationRequest request, Long companyId) {
        Reservation reservation = reservationRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervasyon bulunamadı."));
        assertReservationCompany(reservation, companyId);
        reservationRepository.delete(reservation);
    }

    @Transactional
    public ReservationDto getReservationById(Long id, Long companyId) {
        // Bu rezervasyona bağlı odaları JOIN FETCH ile getir (tüm odalar değil)
        List<Reservation> withRooms = reservationRepository.findWithRoomsByReservationIds(List.of(id));
        Reservation reservation = withRooms.stream()
                .findFirst()
                .orElseGet(() -> reservationRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervasyon bulunamadı.")));

        if (reservation.getGuests() != null) {
            reservation.getGuests().size();
        }
        if (reservation.getPaymentList() != null) {
            reservation.getPaymentList().size();
        }
        assertReservationCompany(reservation, companyId);

        ReservationDto dto = reservationMapper.toDto(reservation);
        kbsNoticeRepository.findFirstByReservationIdOrderByCreatedAtDesc(reservation.getId())
                .ifPresent(notice -> dto.setKbsWarning(notice.getMessage()));
        return dto;
    }

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    @Transactional
    public List<ReservationDto> getAllWithGuests(Long companyId) {
        List<Reservation> reservations = reservationRepository.findAllWithGuestsByCompanyId(companyId);
        loadRoomsForReservations(reservations);
        return reservations.stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    private void loadRoomsForReservations(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            return;
        }
        List<Long> reservationIds = reservations.stream()
                .map(Reservation::getId)
                .toList();
        List<Reservation> withRooms = reservationRepository.findWithRoomsByReservationIds(reservationIds);
        // Persistence context'te oda koleksiyonları yüklensin diye eriş
        withRooms.forEach(r -> {
            if (r.getRooms() != null) {
                r.getRooms().size();
            }
        });
    }

    @Transactional
    public void addPayment(Long reservationId, CreatePaidAmountRequest request, Long companyId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervasyon bulunamadı."));
        assertReservationCompany(reservation, companyId);

        Payment payment = new Payment();
        payment.setPaidAmount(request.getPaidAmount());
        payment.setClosePaymentType(request.getClosePaymentType());
        payment.setPaymentDate(request.getPaymentDate());

        // ilişkiyi bağla
        payment.setReservation(reservation);

        // listeye ekle (silmeden)
        reservation.getPaymentList().add(payment);

        reservationRepository.save(reservation);
    }

    private KbsGuestCheckInRequest getGuestForKBS(Guest guest) throws Exception {
        KbsGuestCheckInRequest kbsGuestCheckInRequest = new KbsGuestCheckInRequest();

        MusteriKimlikNoGirisTalep musteriKimlikNoGirisTalep = new MusteriKimlikNoGirisTalep();
        musteriKimlikNoGirisTalep.setKIMLIKNO(Long.parseLong(guest.getIdentityNumber()));
        musteriKimlikNoGirisTalep.setGRSTRH(convert(LocalDateTime.now()));
        musteriKimlikNoGirisTalep.setKULLANIMSEKLI(SnfEnumKonaklayanKullanimSekli.KONAKLAMA);
        // Veriler varsa doldur
        if (guest.getCountryCode() != null) {
            musteriKimlikNoGirisTalep.setULKKOD(guest.getCountryCode());
        }
        if (guest.getGuestUsageType() != null) {
            musteriKimlikNoGirisTalep.setKULLANIMSEKLI(guest.getGuestUsageType());
        }
        // TELNO ve PLKNO generated client'ta JAXBElement<String> istediği için burada set edilmiyor

        kbsGuestCheckInRequest.setMusteri(musteriKimlikNoGirisTalep);
        return kbsGuestCheckInRequest;
    }

    public XMLGregorianCalendar convert(LocalDateTime localDateTime) throws Exception {

        GregorianCalendar calendar = GregorianCalendar.from(
                localDateTime.atZone(ZoneId.systemDefault())
        );

        return DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(calendar);
    }
}