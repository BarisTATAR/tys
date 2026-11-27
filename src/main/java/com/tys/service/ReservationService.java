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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final KbsCredentialService kbsCredentialService;
    private final KbsNoticeRepository kbsNoticeRepository;
    private final ObjectProvider<KbsNotificationService> kbsNotifications;
    private final String RESERVATION_MADE_MESSAGE = " rezervasyonunuz başarıyla oluşturulmuştur.";
    private final String CHECK_IN_DONE_MESSAGE = " giriş işlemleriniz başarıyla tamamlanmıştır.";
    private final String CHECK_OUT_DONE_MESSAGE = " çıkış işlemleriniz başarıyla tamamlanmıştır. Görüşleriniz bizim için değerli, bizi değerlendirmeyi unutmayın.";

    public ReservationService(ReservationRepository reservationRepository, ReservationMapper reservationMapper, GuestMapper guestMapper, RoomRepository roomRepository, GuestRepository guestRepository, PaymentRepository paymentRepository, CompanyRepository companyRepository, ReservationCafeItemService reservationCafeItemService, RoomService roomService, VatanSmsService vatanSmsService, KbsService kbsService, KbsCredentialService kbsCredentialService, KbsNoticeRepository kbsNoticeRepository, ObjectProvider<KbsNotificationService> kbsNotifications) {
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
        this.kbsCredentialService = kbsCredentialService;
        this.kbsNoticeRepository = kbsNoticeRepository;
        this.kbsNotifications = kbsNotifications;
    }

    @Transactional
    public String updateReservation(UpdateReservationRequest request, Long companyId) throws Exception {
        Reservation existingReservation = reservationRepository.findById(request.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervasyon bulunamadı."));
        assertReservationCompany(existingReservation, companyId);
        if (existingReservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("İptal edilmiş rezervasyon güncellenemez.");
        }
        if (request.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("Rezervasyonu iptal etmek için silme işlemini kullanın.");
        }
        assertStatusTransition(existingReservation.getReservationStatus(), request.getReservationStatus());
        validateUpdateInput(request, companyId);
        if (request.getReservationMessage() != null) {
            InputRules.plainText(request.getReservationMessage(), "Açıklama", InputRules.MAX_TEXT, false);
        }
        LocalDate previousCheckIn = ReservationDateUtils.toLocalDate(existingReservation.getCheckInDate());
        LocalDate previousCheckOut = ReservationDateUtils.toLocalDate(existingReservation.getCheckOutDate());
        boolean checkingOut = request.getReservationStatus() == ReservationStatus.CHECK_OUT_DONE
                && existingReservation.getReservationStatus() != ReservationStatus.CHECK_OUT_DONE;
        boolean checkingIn = request.getReservationStatus() == ReservationStatus.CHECK_IN_DONE
                && existingReservation.getReservationStatus() != ReservationStatus.CHECK_IN_DONE;
        // Check-in yapılmış rezervasyonda KBS'ye girişi bildirilmiş misafirler korunur (çıkarılamaz, TCKN'si değişmez)
        boolean wasCheckedIn = existingReservation.getReservationStatus() == ReservationStatus.CHECK_IN_DONE;
        boolean staysCheckedIn = wasCheckedIn && !checkingOut;
        if (wasCheckedIn && request.getGuests() != null) {
            assertStayingGuestsKept(existingReservation, request.getGuests());
        }
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
            assertCheckInDateReached(request, existingReservation);
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
        if (request.getReservationMessage() != null) {
            existingReservation.setReservationMessage(request.getReservationMessage().trim());
        }
        if (request.getCheckInDate() != null) {
            existingReservation.setCheckInDate(ReservationDateUtils.toStartOfDay(request.getCheckInDate()));
        }
        if (request.getCheckOutDate() != null) {
            existingReservation.setCheckOutDate(ReservationDateUtils.toStartOfDay(request.getCheckOutDate()));
        }
        LocalDate newCheckIn = ReservationDateUtils.toLocalDate(existingReservation.getCheckInDate());
        LocalDate newCheckOut = ReservationDateUtils.toLocalDate(existingReservation.getCheckOutDate());
        boolean datesChanged = !Objects.equals(previousCheckIn, newCheckIn) || !Objects.equals(previousCheckOut, newCheckOut);
        // Aynı gün giriş-çıkış (günlük kullanım) serbest; yalnızca çıkış < giriş engellenir
        if (datesChanged && (newCheckIn == null || newCheckOut == null || newCheckOut.isBefore(newCheckIn))) {
            throw new IllegalArgumentException("Geçerli giriş/çıkış tarihleri gereklidir.");
        }
        // Check-out yapılmış rezervasyon odayı işgal etmez; tarih değişince diğer durumlarda mevcut odalar da yeniden denetlenir
        boolean recheckCurrentRooms = datesChanged
                && existingReservation.getReservationStatus() != ReservationStatus.CHECK_OUT_DONE;
        if (request.getGuests() != null) {
            if (existingReservation.getGuests() != null) {
                existingReservation.getGuests().size();
            }
            syncReservationGuests(existingReservation, request.getGuests());
        }
        List<Guest> kbsCheckOutTargets = new ArrayList<>();
        if (checkingOut && existingReservation.getGuests() != null) {
            for (Guest guest : existingReservation.getGuests()) {
                if (isAddedGuest(guest) && guest.getCheckInDate() == null && guest.getCheckOutDate() == null) {
                    // Girişi bildirilmemiş kişi için KBS'ye çıkış gönderilemez
                    throw new IllegalArgumentException(
                            "Check-out ile birlikte yeni misafir eklenemez. Önce misafiri ekleyip kaydedin (KBS'ye giriş bildirilir), sonra check-out yapın.");
                }
            }
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
        // Konaklama sürerken eklenen misafir: girişi yazılır ve KBS'ye giriş bildirilir
        List<Guest> lateArrivals = new ArrayList<>();
        if (staysCheckedIn && existingReservation.getGuests() != null) {
            LocalDateTime arrivedAt = LocalDateTime.now();
            for (Guest guest : existingReservation.getGuests()) {
                if (isAddedGuest(guest) && guest.getCheckInDate() == null && guest.getCheckOutDate() == null) {
                    guest.setCheckInDate(arrivedAt);
                    lateArrivals.add(guest);
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
            syncReservationRooms(existingReservation, request.getRoomList(), recheckCurrentRooms);
        } else if (recheckCurrentRooms) {
            assertCurrentRoomsAvailable(existingReservation);
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
        if (!lateArrivals.isEmpty()) {
            queueKbs(existingReservation.getId(), lateArrivals, existingReservation, true);
            return KbsNotificationService.QUEUED;
        }
        return null;
    }

    /** Girişi KBS'ye bildirilmiş, çıkışı yapılmamış misafir. */
    private boolean isStayingGuest(Guest guest) {
        return isAddedGuest(guest) && guest.getCheckInDate() != null && guest.getCheckOutDate() == null;
    }

    static final String STAYING_GUEST_TCKN_LOCKED =
            "KBS'ye girişi bildirilmiş misafirin TCKN'si değiştirilemez. Yanlış girildiyse misafire erken check-out yapıp doğru TCKN ile yeniden ekleyin.";

    /**
     * Check-in yapılmış rezervasyonda konaklayan misafir güncellemeyle listeden çıkarılamaz (KBS'ye çıkış gitmezdi)
     * ve TCKN'si değiştirilemez (çıkış bildirimi KBS'deki giriş kaydıyla eşleşmezdi).
     */
    private void assertStayingGuestsKept(Reservation reservation, List<Guest> requestGuests) {
        if (reservation.getGuests() == null) {
            return;
        }
        Map<Long, Guest> incomingById = new HashMap<>();
        for (Guest incoming : requestGuests) {
            if (incoming != null && incoming.getId() != null) {
                incomingById.put(incoming.getId(), incoming);
            }
        }
        for (Guest staying : reservation.getGuests()) {
            if (!isStayingGuest(staying)) {
                continue;
            }
            Guest incoming = incomingById.get(staying.getId());
            if (incoming == null) {
                throw new IllegalArgumentException(
                        "Check-in yapılmış misafir listeden çıkarılamaz (KBS'ye giriş bildirildi). Misafir ayrılıyorsa erken check-out yapın.");
            }
            if (identityChanged(staying.getIdentityNumber(), incoming.getIdentityNumber())) {
                throw new IllegalArgumentException(STAYING_GUEST_TCKN_LOCKED);
            }
        }
    }

    /** Kayıtlı TCKN varken farklı bir TCKN gelmesi (boş gelen değer değişiklik sayılmaz). */
    static boolean identityChanged(String stored, String incoming) {
        if (incoming == null || incoming.isBlank() || stored == null || stored.isBlank()) {
            return false;
        }
        return !stored.trim().equals(incoming.trim());
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
    private void validateUpdateInput(UpdateReservationRequest request, Long companyId) {
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
                Guest stored = guestRepository.findById(guest.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Misafir bulunamadı."));
                assertGuestCompany(stored, companyId);
                previousIdentity = stored.getIdentityNumber();
                previousPhone = stored.getPhoneNumber();
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

    /** Check-in, giriş gününden önce yapılamaz (erken varışta önce rezervasyon tarihi güncellenir). */
    private void assertCheckInDateReached(UpdateReservationRequest request, Reservation existingReservation) {
        LocalDateTime checkIn = request.getCheckInDate() != null ? request.getCheckInDate() : existingReservation.getCheckInDate();
        LocalDate checkInDay = ReservationDateUtils.toLocalDate(checkIn);
        if (checkInDay != null && LocalDate.now().isBefore(checkInDay)) {
            throw new IllegalArgumentException("Giriş tarihinden (" + checkInDay.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ") önce check-in yapılamaz.");
        }
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
            throw new IllegalArgumentException(
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
        Company company = reservation.getCompany();
        Long companyId = company != null ? company.getId() : null;
        List<Guest> managedGuests = new ArrayList<>();
        for (Guest incoming : requestGuests) {
            if (incoming == null) {
                continue;
            }
            Guest managed;
            if (incoming.getId() != null) {
                managed = guestRepository.findById(incoming.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Misafir bulunamadı."));
                // Başka işletmenin misafiri id ile bu rezervasyona bağlanamaz / değiştirilemez
                assertGuestCompany(managed, companyId);
                if (managed.getCompany() == null) {
                    managed.setCompany(company);
                }
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
                // İstekle gelen company/reservations alanlarına güvenilmez; yeni misafir rezervasyonun işletmesine aittir
                incoming.setCompany(company);
                incoming.setReservations(new ArrayList<>());
                // Giriş/çıkış tarihleri KBS bildirim durumunu gösterir; yalnız sunucu yazar
                incoming.setCheckInDate(null);
                incoming.setCheckOutDate(null);
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

    /**
     * Misafir bu işletmeye aitse (işletme kaydı ya da işletmenin bir rezervasyonu üzerinden) geçer; değilse 403.
     */
    private void assertGuestCompany(Guest guest, Long companyId) {
        if (companyId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
        }
        if (guest.getCompany() != null) {
            if (companyId.equals(guest.getCompany().getId())) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
        }
        if (guest.getReservations() != null) {
            for (Reservation linked : guest.getReservations()) {
                if (linked.getCompany() != null && companyId.equals(linked.getCompany().getId())) {
                    return;
                }
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
    }

    /**
     * Oda listesi gönderilmeden tarih değiştirildiğinde rezervasyondaki odaların yeni tarihlerde boş olduğunu doğrular.
     */
    private void assertCurrentRoomsAvailable(Reservation reservation) {
        if (reservation.getRooms() == null || reservation.getRooms().isEmpty()) {
            return;
        }
        Long companyId = reservation.getCompany() != null ? reservation.getCompany().getId() : null;
        if (companyId == null) {
            throw new IllegalArgumentException("Rezervasyonun işletme bilgisi bulunamadı.");
        }
        LocalDate reqIn = ReservationDateUtils.toLocalDate(reservation.getCheckInDate());
        LocalDate reqOut = ReservationDateUtils.toLocalDate(reservation.getCheckOutDate());
        lockRooms(reservation.getRooms().stream().filter(Objects::nonNull).map(Room::getId).toList());
        List<Long> availableIds = roomService
                .getAvailableRoomsExcludingReservation(reqIn.atStartOfDay(), reqOut.atStartOfDay(), companyId, reservation.getId())
                .stream()
                .map(RoomDto::getId)
                .toList();
        for (Room room : reservation.getRooms()) {
            if (room != null && !availableIds.contains(room.getId())) {
                throw new IllegalArgumentException(
                        "Oda " + room.getNumber() + " seçilen tarihlerde müsait değil (başka rezervasyon var).");
            }
        }
    }

    /**
     * @param recheckCurrentRooms true ise rezervasyonda zaten bulunan odalar da müsaitlik denetiminden geçer
     *                            (tarih değiştiğinde zorunludur; yeni rezervasyonda etkisizdir).
     */
    private void syncReservationRooms(Reservation reservation, List<CreateRoomRequest> roomRequests, boolean recheckCurrentRooms) {
        if (roomRequests == null || roomRequests.isEmpty()) {
            throw new IllegalArgumentException("Rezervasyonda en az bir oda olmalıdır.");
        }
        if (reservation.getCompany() == null || reservation.getCompany().getId() == null) {
            throw new IllegalArgumentException("Rezervasyonun işletme bilgisi bulunamadı.");
        }

        Long companyId = reservation.getCompany().getId();
        LocalDate reqIn = ReservationDateUtils.toLocalDate(reservation.getCheckInDate());
        LocalDate reqOut = ReservationDateUtils.toLocalDate(reservation.getCheckOutDate());
        // Aynı gün giriş-çıkış (günlük kullanım) serbest; yalnızca çıkış < giriş engellenir
        if (reqIn == null || reqOut == null || reqOut.isBefore(reqIn)) {
            throw new IllegalArgumentException("Geçerli giriş/çıkış tarihleri gereklidir.");
        }

        List<Room> currentRooms = reservation.getRooms() != null
                ? new ArrayList<>(reservation.getRooms())
                : new ArrayList<>();
        List<Long> currentRoomIds = currentRooms.stream().map(Room::getId).toList();

        // 1) İstenen odaları çöz
        List<Room> newRooms = new ArrayList<>();
        for (CreateRoomRequest roomFromRequest : roomRequests) {
            if (roomFromRequest == null || roomFromRequest.getNumber() == null) {
                throw new IllegalArgumentException("Oda numarası boş olamaz.");
            }
            Room room = roomRepository.findByNumberAndCompanyId(roomFromRequest.getNumber(), companyId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Oda bulunamadı: " + roomFromRequest.getNumber() + "."));
            if (newRooms.stream().noneMatch(r -> r.getId().equals(room.getId()))) {
                newRooms.add(room);
            }
        }
        if (newRooms.isEmpty()) {
            throw new IllegalArgumentException("Rezervasyonda en az bir oda olmalıdır.");
        }

        // 2) Odaları kilitle: aynı odaya eşzamanlı gelen istekler sıraya girer
        lockRooms(newRooms.stream().map(Room::getId).toList());

        // 3) Müsaitliği kilit altında, gün bazlı denetle (mevcut rezervasyon hariç)
        List<Long> availableIds = roomService
                .getAvailableRoomsExcludingReservation(
                        reqIn.atStartOfDay(),
                        reqOut.atStartOfDay(),
                        companyId,
                        reservation.getId())
                .stream()
                .map(RoomDto::getId)
                .toList();
        for (Room room : newRooms) {
            boolean alreadyOnReservation = currentRoomIds.contains(room.getId());
            boolean available = availableIds.contains(room.getId());
            if (!available && (!alreadyOnReservation || recheckCurrentRooms)) {
                throw new IllegalArgumentException(
                        "Oda " + room.getNumber() + " seçilen tarihlerde müsait değil (başka rezervasyon var).");
            }
        }

        if (reservation.getRooms() == null) {
            reservation.setRooms(new ArrayList<>());
        } else {
            reservation.getRooms().clear();
        }
        reservation.getRooms().addAll(newRooms);
    }

    /** Oda satırlarını işlem sonuna kadar kilitler (SELECT … FOR UPDATE, id sırasıyla). */
    private void lockRooms(List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return;
        }
        roomRepository.lockAllByIdIn(new java.util.TreeSet<>(roomIds));
    }

    /**
     * İzin verilen durum geçişleri: Rezervasyon Yapıldı ↔ Misafir Bilgileri Girildi → Check-in → Check-out.
     * Check-in geri alınamaz (KBS'ye giriş bildirilmiştir), check-out yapılmış kayıt yeniden açılamaz,
     * check-in yapılmadan check-out yapılamaz.
     */
    private void assertStatusTransition(ReservationStatus from, ReservationStatus to) {
        if (to == null || from == null || to == from) {
            return;
        }
        if (from == ReservationStatus.CHECK_OUT_DONE) {
            throw new IllegalArgumentException("Check-out yapılmış rezervasyonun durumu değiştirilemez.");
        }
        if (to == ReservationStatus.CHECK_OUT_DONE && from != ReservationStatus.CHECK_IN_DONE) {
            throw new IllegalArgumentException("Check-out için önce check-in yapılmalıdır.");
        }
        if (from == ReservationStatus.CHECK_IN_DONE && to != ReservationStatus.CHECK_OUT_DONE) {
            throw new IllegalArgumentException(
                    "Check-in yapılmış rezervasyon önceki duruma alınamaz (KBS'ye giriş bildirildi). Misafir ayrılıyorsa check-out yapın.");
        }
    }

    /**
     * KBS reddi otel kaydını geri almaz. Bildirilemeyen kişiler mesaj olarak döner.
     */
    public String notifyGuestsForKbs(Long companyId, List<Guest> guests, boolean checkIn) {
        if (guests == null || guests.isEmpty()) {
            return null;
        }
        // Her işletme kendi KBS tesis hesabıyla bildirim yapar; bilgiler yoksa ağa çıkılmaz.
        java.util.Optional<KbsCredentials> credentials = kbsCredentialService.find(companyId);
        if (credentials.isEmpty()) {
            return KbsCredentialService.NOT_CONFIGURED;
        }
        return notifyKbs(credentials.get(), guests, checkIn);
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
        Long companyId = reservation.getCompany() != null ? reservation.getCompany().getId() : null;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kbsNotifications.getObject().dispatch(reservationId, companyId, frozen, checkIn);
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

    private String notifyKbs(KbsCredentials credentials, List<Guest> guests, boolean checkIn) {
        if (guests == null || guests.isEmpty()) {
            return null;
        }
        StringBuilder warnings = new StringBuilder();
        for (Guest guest : guests) {
            try {
                if (checkIn) {
                    checkInGuestOnKbs(credentials, guest);
                } else {
                    checkOutGuestOnKbs(credentials, guest);
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
            throw new IllegalArgumentException("Erken check-out için önce check-in yapılmış olmalıdır.");
        }
        if (reservation.getGuests() != null) {
            reservation.getGuests().size();
        }

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Misafir bulunamadı."));
        boolean onReservation = reservation.getGuests() != null
                && reservation.getGuests().stream().anyMatch(existing -> guestId.equals(existing.getId()));
        if (!onReservation) {
            throw new IllegalArgumentException("Bu misafir rezervasyona bağlı değil.");
        }
        if (guest.getCheckOutDate() != null) {
            throw new IllegalArgumentException("Bu misafir zaten çıkış yapmış.");
        }

        if (request != null) {
            if (hasText(request.getName())) {
                guest.setName(request.getName().trim());
            }
            if (hasText(request.getSurname())) {
                guest.setSurname(request.getSurname().trim());
            }
            if (hasText(request.getIdentityNumber())) {
                // KBS çıkış bildirimine hatalı kimlik numarası gitmesin; girişte bildirilen numara değiştirilemez
                InputRules.requireTckn(request.getIdentityNumber().trim());
                if (guest.getCheckInDate() != null && identityChanged(guest.getIdentityNumber(), request.getIdentityNumber())) {
                    throw new IllegalArgumentException(STAYING_GUEST_TCKN_LOCKED);
                }
                guest.setIdentityNumber(request.getIdentityNumber().trim());
            }
            InputRules.personName(guest.getName(), "Misafir adı");
            InputRules.personName(guest.getSurname(), "Misafir soyadı");
        }
        if (!isAddedGuest(guest)) {
            throw new IllegalArgumentException("Erken check-out için misafirin adı ve soyadı girilmiş olmalıdır.");
        }

        LocalDate today = LocalDate.now();
        LocalDate checkIn = ReservationDateUtils.toLocalDate(reservation.getCheckInDate());
        if (checkIn != null && today.isBefore(checkIn)) {
            throw new IllegalArgumentException("Erken check-out için çıkış tarihi giriş tarihinden önce olamaz.");
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

    private void checkInGuestOnKbs(KbsCredentials credentials, Guest guest) throws Exception {
        String identity = guest.getIdentityNumber() == null ? "" : guest.getIdentityNumber().replaceAll("\\D", "");
        String guestName = guest.getName().trim() + " " + guest.getSurname().trim();
        if (identity.isEmpty()) {
            throw new IllegalArgumentException(guestName + " için T.C. kimlik numarası olmadan KBS girişi yapılamadı.");
        }
        try {
            Long.parseLong(identity);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(guestName + " için T.C. kimlik numarası geçersiz.");
        }
        KbsGuestCheckInResponse response = kbsService.checkInGuest(credentials, getGuestForKBS(guest));
        if (response == null || response.getMusteri() == null) {
            throw new IllegalArgumentException(guestName + " için KBS girişi yapılamadı.");
        }
    }

    private void checkOutGuestOnKbs(KbsCredentials credentials, Guest guest) throws Exception {
        String identity = guest.getIdentityNumber() == null ? "" : guest.getIdentityNumber().replaceAll("\\D", "");
        String guestName = guest.getName().trim() + " " + guest.getSurname().trim();
        if (identity.isEmpty()) {
            throw new IllegalArgumentException(guestName + " için T.C. kimlik numarası olmadan KBS çıkışı yapılamaz.");
        }
        long kimlikNo;
        try {
            kimlikNo = Long.parseLong(identity);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(guestName + " için T.C. kimlik numarası geçersiz.");
        }

        ObjectFactory factory = new ObjectFactory();
        MusteriKimlikNoCikisTalep talep = factory.createMusteriKimlikNoCikisTalep();
        talep.setCKSTIP(SnfEnumKonaklayanCikisTurleri.TESISTENCIKIS);
        talep.setCKSTRH(factory.createMusteriKimlikNoCikisTalepCKSTRH(convert(LocalDateTime.now())));
        talep.setKIMLIKNO(factory.createMusteriKimlikNoCikisTalepKIMLIKNO(kimlikNo));

        KbsGuestCheckOutRequest kbsRequest = new KbsGuestCheckOutRequest();
        kbsRequest.setMusteri(talep);
        KbsGuestCheckOutResponse response = kbsService.checkOutGuest(credentials, kbsRequest);
        Sonuc sonuc = response != null ? response.getSonuc() : null;
        if (sonuc == null || !Boolean.TRUE.equals(sonuc.isBasarili())) {
            throw new IllegalArgumentException(guestName + " için KBS çıkışı yapılamadı. " + kbsResultMessage(sonuc));
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
        // Rezervasyona bağlı tüm ödemeleri oluşturulma sırasıyla al; ilk kayıt ana ödemedir (toplam tutar + kapora)
        List<Payment> payments = paymentRepository.findAllByReservationIdOrderByIdAsc(existingReservation.getId());
        if (payments == null || payments.isEmpty()) {
            throw new IllegalArgumentException("Ödeme kaydı olmadan check-out yapılamaz.");
        }
        Payment mainPayment = payments.get(0);

        // Ödenen = tüm satırlardaki kaporalar (bölünmüş kapora dahil) + tüm ek ödemeler
        BigDecimal totalPaid = PaymentService.totalPaid(payments);

        // Ana ödemenin tutarı boş veya 0 ise borç 0 kabul edilir.
        BigDecimal reservationAmount = mainPayment.getAmount() != null
                ? mainPayment.getAmount()
                : BigDecimal.ZERO;
        BigDecimal cafeAmount = reservationCafeItemService.getReservationCafeAmountByReservationId(
                existingReservation.getId());
        if (cafeAmount == null) {
            cafeAmount = BigDecimal.ZERO;
        }
        BigDecimal totalAmount = reservationAmount.add(cafeAmount);

        // Borç devam ediyorsa checkout’a izin verme
        if (totalPaid.compareTo(totalAmount) < 0) {
            throw new IllegalArgumentException(
                    "Tüm borçlar kapatılmadan Check-out yapılamaz. " +
                            "Ödenen Tutar: " + totalPaid + ", Ödenecek Tutar: " + (totalAmount.subtract(totalPaid))
            );
        }
    }

    @Transactional
    public void createReservation(CreateReservationRequest request, Long companyId) {
        if (request.getRoomList() == null || request.getRoomList().isEmpty()) {
            throw new IllegalArgumentException("Rezervasyonda en az bir oda olmalıdır.");
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
            BigDecimal total = null;
            BigDecimal advanceSum = BigDecimal.ZERO;
            for (CreatePaymentRequest payment : request.getPaymentList()) {
                if (payment != null) {
                    InputRules.payment(payment.getAmount(), payment.getAdvancePayment());
                    if (payment.getAdvancePayment() != null && payment.getAdvancePayment().signum() > 0
                            && payment.getAdvancePaymentType() == null) {
                        throw new IllegalArgumentException("Kapora için ödeme şekli seçilmelidir.");
                    }
                    if (total == null && payment.getAmount() != null) {
                        total = payment.getAmount();
                    }
                    if (payment.getAdvancePayment() != null) {
                        advanceSum = advanceSum.add(payment.getAdvancePayment());
                    }
                }
            }
            // Bölünmüş kapora: tüm satırlardaki kaporaların toplamı toplam tutarı geçemez
            if (total != null && advanceSum.compareTo(total) > 0) {
                throw new IllegalArgumentException("Toplam tutar kapora tutarlarının toplamından küçük olamaz.");
            }
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));

        ReservationStatus initialStatus = request.getReservationStatus() == null
                ? ReservationStatus.RESERVATION_MADE
                : request.getReservationStatus();
        if (initialStatus != ReservationStatus.RESERVATION_MADE && initialStatus != ReservationStatus.GUEST_INFORMATION_ENTERED) {
            // Check-in/out yalnız güncelleme akışında yapılır: misafir adı kuralları ve KBS bildirimi atlanamaz
            throw new IllegalArgumentException("Yeni rezervasyon yalnız \"Rezervasyon Yapıldı\" durumuyla oluşturulabilir; check-in rezervasyon güncellenerek yapılır.");
        }
        request.setReservationStatus(initialStatus);
        Reservation reservation = reservationMapper.createReservationRequestToEntity(request);
        reservation.setCompany(company);
        // Kayıt tarihi sunucu saatinden (İstanbul) alınır; istemcinin UTC'ye göre gönderdiği tarihe güvenilmez
        reservation.setReservationDate(LocalDate.now().atStartOfDay());
        // Mapper oda listesini doldurmuş olabilir; müsaitlik/tarih kuralları sync içinde uygulanır
        if (reservation.getRooms() != null) {
            reservation.getRooms().clear();
        }

        // P0: tarih (checkOut > checkIn) + oda çakışması + min 1 oda
        syncReservationRooms(reservation, request.getRoomList(), true);
        InputRules.stayCounts(
                request.getAdultGuestNumber(),
                request.getChildGuestNumber(),
                request.getBabyGuestNumber(),
                totalRoomCapacity(reservation));

        if (request.getGuestList() != null && !request.getGuestList().isEmpty()) {
            List<Guest> guests = guestMapper.toEntityList(request.getGuestList());
            // Misafir sahipliği: rezervasyonla eklenen misafir işletmeye aittir (tenant kontrolleri bu alana dayanır)
            guests.forEach(guest -> guest.setCompany(company));
            reservation.setGuests(guests);
        } else {
            reservation.setGuests(new ArrayList<>());
        }

        reservationRepository.save(reservation);
    }

    /**
     * Ödemesi olmayan rezervasyon silinir. Ödemesi olan rezervasyon mali kayıtlar korunarak iptal edilir (CANCELLED)
     * ve odayı işgal etmez. Check-in yapılmış veya tamamlanmış konaklama silinemez.
     *
     * @return kullanıcıya gösterilecek sonuç mesajı
     */
    @Transactional
    public String deleteReservation(DeleteReservationRequest request, Long companyId) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Silinecek rezervasyon seçilmelidir.");
        }
        Reservation reservation = reservationRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervasyon bulunamadı."));
        assertReservationCompany(reservation, companyId);
        ReservationStatus status = reservation.getReservationStatus();
        if (status == ReservationStatus.CHECK_IN_DONE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Check-in yapılmış rezervasyon silinemez; önce check-out yapın.");
        }
        if (status == ReservationStatus.CHECK_OUT_DONE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tamamlanmış konaklama kaydı silinemez.");
        }
        if (status == ReservationStatus.CANCELLED) {
            return "Rezervasyon zaten iptal edilmiş.";
        }
        if (!paymentRepository.findAllByReservationIdOrderByIdAsc(reservation.getId()).isEmpty()) {
            reservation.setReservationStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            return "Rezervasyonun ödeme kaydı olduğu için silinmedi, iptal edildi. Ödeme kayıtları korunur ve oda boşaltıldı.";
        }
        reservationRepository.delete(reservation);
        return "Rezervasyon silindi.";
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
                .ifPresent(notice -> {
                    dto.setKbsWarning(notice.getMessage());
                    dto.setKbsWarningId(notice.getId());
                });
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

        if (request == null) {
            throw new IllegalArgumentException("Ödeme bilgisi gereklidir.");
        }
        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("İptal edilmiş rezervasyona ödeme eklenemez.");
        }
        InputRules.paidAmount(request.getPaidAmount(), request.getClosePaymentType());

        Payment payment = new Payment();
        payment.setPaidAmount(request.getPaidAmount());
        payment.setClosePaymentType(request.getClosePaymentType());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());

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