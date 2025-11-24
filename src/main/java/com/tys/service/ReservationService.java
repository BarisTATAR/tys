package com.tys.service;

import com.tys.dto.ReservationDto;
import com.tys.enums.ReservationStatus;
import com.tys.mapper.GuestMapper;
import com.tys.mapper.ReservationMapper;
import com.tys.model.Payment;
import com.tys.model.Reservation;
import com.tys.model.Room;
import com.tys.model.Guest;
import com.tys.repository.PaymentRepository;
import com.tys.repository.ReservationRepository;
import com.tys.repository.RoomRepository;
import com.tys.request.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final GuestMapper guestMapper;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationCafeItemService reservationCafeItemService;
    public ReservationService(ReservationRepository reservationRepository, ReservationMapper reservationMapper, GuestMapper guestMapper, RoomRepository roomRepository, PaymentRepository paymentRepository, ReservationCafeItemService reservationCafeItemService) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.guestMapper = guestMapper;
        this.roomRepository = roomRepository;
        this.paymentRepository = paymentRepository;
        this.reservationCafeItemService = reservationCafeItemService;
    }
    public void updateReservation(UpdateReservationRequest request) {

        Reservation existingReservation = reservationRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Reservation not found with Id: " + request.getId()));
        // 1️⃣ Eğer status CHECKOUT yapılmak isteniyorsa borç kontrolü yap

        if (request.getReservationStatus() == ReservationStatus.CHECK_OUT_DONE) {
            isAllPaymentsCompleted(request, existingReservation);
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
    public void createReservation(CreateReservationRequest request) {
        Reservation reservation = reservationMapper.createReservationRequestToEntity(request);

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

    public List<ReservationDto> getAllWithGuests() {
        List<Reservation> reservations = reservationRepository.findAllWithGuests();
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

}