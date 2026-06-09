package com.rasirom.booking_service.service;

import com.rasirom.booking_service.dto.CancelReservationRequest;
import com.rasirom.booking_service.dto.CreateReservationRequest;
import com.rasirom.booking_service.dto.ReservationResponse;
import com.rasirom.booking_service.event.ReservationEvent;
import com.rasirom.booking_service.event.ReservationEventType;
import com.rasirom.booking_service.model.Desk;
import com.rasirom.booking_service.model.Reservation;
import com.rasirom.booking_service.model.ReservationStatus;
import com.rasirom.booking_service.repository.DeskRepository;
import com.rasirom.booking_service.repository.ReservationRepository;
import com.rasirom.booking_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final DeskRepository deskRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, ReservationEvent> kafkaTemplate;
    private final String reservationsTopic;

    public ReservationService(ReservationRepository reservationRepository,
                              DeskRepository deskRepository,
                              UserRepository userRepository,
                              KafkaTemplate<String, ReservationEvent> kafkaTemplate,
                              @Value("${kafka.topics.reservations}") String reservationsTopic) {
        this.reservationRepository = reservationRepository;
        this.deskRepository = deskRepository;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.reservationsTopic = reservationsTopic;
    }

    @Transactional
    public ReservationResponse create(Long userId, CreateReservationRequest request) {
        Desk desk = deskRepository.findByIdAndActiveTrue(request.getDeskId())
                .orElseThrow(() -> new IllegalArgumentException("Desk not found or inactive"));

        if (reservationRepository.existsByUserIdAndDayAndStatus(
                userId, request.getDay(), ReservationStatus.ACTIVE)) {
            throw new IllegalStateException("You already have an active reservation for that day");
        }

        if (reservationRepository.existsByDeskIdAndDayAndStatus(
                request.getDeskId(), request.getDay(), ReservationStatus.ACTIVE)) {
            throw new IllegalStateException("Desk is already reserved for that day");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(userRepository.getReferenceById(userId));
        reservation.setDesk(desk);
        reservation.setDay(request.getDay());

        Reservation saved = reservationRepository.save(reservation);

        kafkaTemplate.send(reservationsTopic, String.valueOf(saved.getId()),
                new ReservationEvent(
                        ReservationEventType.CREATED,
                        saved.getId(), userId, desk.getId(),
                        desk.getDeskNumber(), desk.getRoomNumber(), desk.getFloor(),
                        saved.getDay(), saved.getCreatedAt()
                ));

        return new ReservationResponse(
                saved.getId(),
                userId,
                desk.getId(),
                desk.getDeskNumber(),
                desk.getRoomNumber(),
                desk.getFloor(),
                saved.getDay(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public ReservationResponse cancel(Long userId, Long reservationId, CancelReservationRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not allowed to cancel this reservation");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation is already cancelled");
        }

        if (!reservation.getDay().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Only future reservations can be cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation.setCancellationReason(request.getReason());

        Reservation saved = reservationRepository.save(reservation);
        Desk desk = saved.getDesk();

        kafkaTemplate.send(reservationsTopic, String.valueOf(saved.getId()),
                new ReservationEvent(
                        ReservationEventType.CANCELLED,
                        saved.getId(), userId, desk.getId(),
                        desk.getDeskNumber(), desk.getRoomNumber(), desk.getFloor(),
                        saved.getDay(), saved.getCreatedAt()
                ));

        return new ReservationResponse(
                saved.getId(),
                userId,
                desk.getId(),
                desk.getDeskNumber(),
                desk.getRoomNumber(),
                desk.getFloor(),
                saved.getDay(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByDayDesc(userId).stream()
                .map(r -> new ReservationResponse(
                        r.getId(),
                        r.getUser().getId(),
                        r.getDesk().getId(),
                        r.getDesk().getDeskNumber(),
                        r.getDesk().getRoomNumber(),
                        r.getDesk().getFloor(),
                        r.getDay(),
                        r.getStatus(),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
