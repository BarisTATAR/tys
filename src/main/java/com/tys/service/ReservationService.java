package com.tys.service;

import com.tys.client.MusteriKimlikNoGirisTalep;
import com.tys.client.SnfEnumKonaklayanKullanimSekli;
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
import com.tys.repository.PaymentRepository;
import com.tys.repository.ReservationRepository;
import com.tys.repository.RoomRepository;
import com.tys.request.*;
import com.tys.util.ReservationDateUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
    private final String RESERVATION_MADE_MESSAGE = " rezervasyonunuz başarıyla oluşturulmuştur.";
    private final String CHECK_IN_DONE_MESSAGE = " giriş işlemleriniz başarıyla tamamlanmıştır.";
    private final String CHECK_OUT_DONE_MESSAGE = " çıkış işlemleriniz başarıyla tamamlanmıştır. Görüşleriniz bizim için değerli, bizi değerlendirmeyi unutmayın.";

    public ReservationService(ReservationRepository reservationRepository, ReservationMapper reservationMapper, GuestMapper guestMapper, RoomRepository roomRepository, GuestRepository guestRepository, PaymentRepository paymentRepository, CompanyRepository companyRepository, ReservationCafeItemService reservationCafeItemService, RoomService roomService, VatanSmsService vatanSmsService, KbsService kbsService) {
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
    }

    @Transactional
    public void updateReservation(UpdateReservationRequest request) throws Exception {
        Reservation existingReservation = reservationRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Reservation not found with Id: " + request.getId()));
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
        } else if (request.getReservationStatus() == ReservationStatus.CHECK_IN_DONE) {
//            vatanSmsService.sendSms(new CreateSMSRequest(List.of(request.getGuests().get(0).getPhoneNumber()), "",
//                    contactGuest.getName() + " " + contactGuest.getSurname() + CHECK_IN_DONE_MESSAGE));
//            for (Guest guest : request.getGuests()) {
//                kbsService.checkInGuest(getGuestForKBS(guest));
//            }
        } else if (request.getReservationStatus() == ReservationStatus.CHECK_OUT_DONE) {
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
        if (request.getRoomList() != null) {
            if (existingReservation.getCompany() != null) {
                existingReservation.getCompany().getId();
            }
            if (existingReservation.getRooms() != null) {
                existingReservation.getRooms().size();
            }
            syncReservationRooms(existingReservation, request.getRoomList());
        }
        reservationRepository.save(existingReservation);
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
        if (reqIn == null || reqOut == null || !reqOut.isAfter(reqIn)) {
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

    private void isAllPaymentsCompleted(UpdateReservationRequest request, Reservation existingReservation) {
        // Rezervasyona bağlı tüm ödemeleri al
        List<Payment> payments = paymentRepository.findAllByReservationId(existingReservation.getId());

        // Toplam ödenen tutarı hesapla
        BigDecimal totalPaid = payments.get(0).getAdvancePayment();
        for (Payment p : payments) {
            if (p.getPaidAmount() != null) {
                totalPaid = totalPaid.add(p.getPaidAmount());
            }
        }

        // Toplam borç = reservation totalAmount
        BigDecimal totalAmount = payments.get(0).getAmount().add(reservationCafeItemService.getReservationCafeAmountByReservationId(request.getId()));

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
        Reservation reservation = reservationMapper.createReservationRequestToEntity(request);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
        reservation.setCompany(company);

        List<Room> roomsFromDb = new ArrayList<>();
        for (CreateRoomRequest roomFromRequest : request.getRoomList()) {
            Room room = roomRepository.findByNumberAndCompanyId(roomFromRequest.getNumber(), companyId)
                    .orElseThrow(() -> new RuntimeException(
                            "Oda bulunamadı. Oda no: " + roomFromRequest.getNumber() + ", işletme id: " + companyId));
            roomsFromDb.add(room);
        }
        reservation.setRooms(roomsFromDb);

        List<Guest> guests = guestMapper.toEntityList(request.getGuestList());
        reservation.setGuests(guests);

        reservationRepository.save(reservation);
    }

    public void deleteReservation(DeleteReservationRequest request) {
        if (!reservationRepository.existsById(request.getId())) {
            throw new RuntimeException("Reservation not found with Id: " + request.getId());
        }
        reservationRepository.deleteById(request.getId());
    }

    @Transactional
    public ReservationDto getReservationById(Long id) {
        // Bu rezervasyona bağlı odaları JOIN FETCH ile getir (tüm odalar değil)
        List<Reservation> withRooms = reservationRepository.findWithRoomsByReservationIds(List.of(id));
        Reservation reservation = withRooms.stream()
                .findFirst()
                .orElseGet(() -> reservationRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Reservation not found with Id: " + id)));

        if (reservation.getGuests() != null) {
            reservation.getGuests().size();
        }
        if (reservation.getPaymentList() != null) {
            reservation.getPaymentList().size();
        }

        return reservationMapper.toDto(reservation);
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
    public void addPayment(Long reservationId, CreatePaidAmountRequest request) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

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