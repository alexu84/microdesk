package com.rasirom.booking_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasirom.booking_service.dto.CancelReservationRequest;
import com.rasirom.booking_service.dto.CreateReservationRequest;
import com.rasirom.booking_service.dto.ReservationResponse;
import com.rasirom.booking_service.event.ReservationEvent;
import com.rasirom.booking_service.event.ReservationEventType;
import com.rasirom.booking_service.model.Desk;
import com.rasirom.booking_service.model.OutboxMessage;
import com.rasirom.booking_service.model.Reservation;
import com.rasirom.booking_service.model.ReservationStatus;
import com.rasirom.booking_service.repository.DeskRepository;
import com.rasirom.booking_service.repository.OutboxMessageRepository;
import com.rasirom.booking_service.repository.ReservationRepository;
import com.rasirom.booking_service.repository.UserRepository;
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
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    public ReservationService(ReservationRepository reservationRepository,
                              DeskRepository deskRepository,
                              UserRepository userRepository,
                              OutboxMessageRepository outboxMessageRepository,
                              ObjectMapper objectMapper) {
        this.reservationRepository = reservationRepository;
        this.deskRepository = deskRepository;
        this.userRepository = userRepository;
        this.outboxMessageRepository = outboxMessageRepository;
        this.objectMapper = objectMapper;
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

        saveOutboxMessage(ReservationEventType.CREATED, saved, desk, userId);

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

        saveOutboxMessage(ReservationEventType.CANCELLED, saved, desk, userId);

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

    private void saveOutboxMessage(ReservationEventType eventType, Reservation reservation, Desk desk, Long userId) {
        ReservationEvent event = new ReservationEvent(
                eventType,
                reservation.getId(), userId, desk.getId(),
                desk.getDeskNumber(), desk.getRoomNumber(), desk.getFloor(),
                reservation.getDay(), reservation.getCreatedAt()
        );
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxMessageRepository.save(new OutboxMessage(eventType.name(), payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }
}
