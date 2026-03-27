package com.tys.service;

import com.tys.client.MusteriKimlikNoGirisTalep;
import com.tys.client.SnfEnumKonaklayanKullanimSekli;
import com.tys.dto.ReservationDto;
import com.tys.enums.ReservationStatus;
import com.tys.mapper.GuestMapper;
import com.tys.mapper.ReservationMapper;
import com.tys.model.Company;
import com.tys.model.Payment;
import com.tys.model.Reservation;
import com.tys.model.Room;
import com.tys.model.Guest;
import com.tys.repository.CompanyRepository;
import com.tys.repository.PaymentRepository;
import com.tys.repository.ReservationRepository;
import com.tys.repository.RoomRepository;
import com.tys.request.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
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
    private final PaymentRepository paymentRepository;
    private final CompanyRepository companyRepository;
    private final ReservationCafeItemService reservationCafeItemService;
    private final VatanSmsService vatanSmsService;
    private final KbsService kbsService;
    private final String RESERVATION_MADE_MESSAGE = " rezervasyonunuz başarıyla oluşturulmuştur.";
    private final String CHECK_IN_DONE_MESSAGE = " giriş işlemleriniz başarıyla tamamlanmıştır.";
    private final String CHECK_OUT_DONE_MESSAGE = " çıkış işlemleriniz başarıyla tamamlanmıştır. Görüşleriniz bizim için değerli, bizi değerlendirmeyi unutmayın.";

    public ReservationService(ReservationRepository reservationRepository, ReservationMapper reservationMapper, GuestMapper guestMapper, RoomRepository roomRepository, PaymentRepository paymentRepository, CompanyRepository companyRepository, ReservationCafeItemService reservationCafeItemService, VatanSmsService vatanSmsService, KbsService kbsService) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.guestMapper = guestMapper;
        this.roomRepository = roomRepository;
        this.paymentRepository = paymentRepository;
        this.companyRepository = companyRepository;
        this.reservationCafeItemService = reservationCafeItemService;
        this.vatanSmsService = vatanSmsService;
        this.kbsService = kbsService;
    }

    public void updateReservation(UpdateReservationRequest request) throws Exception {
        Reservation existingReservation = reservationRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Reservation not found with Id: " + request.getId()));
        // 1️⃣ Eğer status CHECKOUT yapılmak isteniyorsa borç kontrolü yap

        Guest contactGuest = new Guest();
        for (Guest guest : request.getGuests()) {
            if (guest.getIsContact()) {
                contactGuest.setName(guest.getName());
                contactGuest.setSurname(guest.getSurname());
                contactGuest.setPhoneNumber(guest.getPhoneNumber());
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
        reservationRepository.save(existingReservation);
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
            roomRepository.findByNumber(roomFromRequest.getNumber())
                    .ifPresent(roomsFromDb::add);
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

    public ReservationDto getReservationById(Long id) {
        Reservation reservation = reservationRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with Id: " + id));

        return reservationMapper.toDto(reservation);
    }

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public List<ReservationDto> getAllWithGuests(Long companyId) {
        List<Reservation> reservations = reservationRepository.findAllWithGuestsByCompanyId(companyId);
        return reservations.stream()
                .map(reservationMapper::toDto)
                .toList();
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