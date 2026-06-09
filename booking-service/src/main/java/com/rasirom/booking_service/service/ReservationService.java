package com.rasirom.booking_service.service;

import com.rasirom.booking_service.dto.CreateReservationRequest;
import com.rasirom.booking_service.dto.ReservationResponse;
import com.rasirom.booking_service.model.Desk;
import com.rasirom.booking_service.model.Reservation;
import com.rasirom.booking_service.model.ReservationStatus;
import com.rasirom.booking_service.repository.DeskRepository;
import com.rasirom.booking_service.repository.ReservationRepository;
import com.rasirom.booking_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final DeskRepository deskRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              DeskRepository deskRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.deskRepository = deskRepository;
        this.userRepository = userRepository;
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
