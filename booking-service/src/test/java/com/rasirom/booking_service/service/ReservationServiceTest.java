package com.rasirom.booking_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasirom.booking_service.dto.CancelReservationRequest;
import com.rasirom.booking_service.dto.CreateReservationRequest;
import com.rasirom.booking_service.dto.ReservationResponse;
import com.rasirom.booking_service.model.*;
import com.rasirom.booking_service.repository.DeskRepository;
import com.rasirom.booking_service.repository.OutboxMessageRepository;
import com.rasirom.booking_service.repository.ReservationRepository;
import com.rasirom.booking_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private DeskRepository deskRepository;
    @Mock private UserRepository userRepository;
    @Mock private OutboxMessageRepository outboxMessageRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private ReservationService reservationService;

    private Desk desk;
    private User user;
    private final Long USER_ID = 1L;
    private final Long DESK_ID = 10L;
    private final LocalDate FUTURE_DAY = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() throws JsonProcessingException {
        desk = new Desk();
        desk.setId(DESK_ID);
        desk.setDeskNumber(5);
        desk.setRoomNumber("A1");
        desk.setFloor(2);

        user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");

        // Default: outbox serialization succeeds (lenient — not all tests reach this path)
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        lenient().when(outboxMessageRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    void create_happyPath_returnsReservationResponse() {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setDeskId(DESK_ID);
        request.setDay(FUTURE_DAY);

        when(deskRepository.findByIdAndActiveTrue(DESK_ID)).thenReturn(Optional.of(desk));
        when(reservationRepository.existsByUserIdAndDayAndStatus(USER_ID, FUTURE_DAY, ReservationStatus.ACTIVE)).thenReturn(false);
        when(reservationRepository.existsByDeskIdAndDayAndStatus(DESK_ID, FUTURE_DAY, ReservationStatus.ACTIVE)).thenReturn(false);
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);

        Reservation saved = buildReservation(1L, user, desk, FUTURE_DAY, ReservationStatus.ACTIVE);
        when(reservationRepository.save(any())).thenReturn(saved);

        ReservationResponse response = reservationService.create(USER_ID, request);

        assertThat(response.getDeskId()).isEqualTo(DESK_ID);
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getDay()).isEqualTo(FUTURE_DAY);
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
    }

    @Test
    void create_deskNotFound_throwsIllegalArgument() {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setDeskId(DESK_ID);
        request.setDay(FUTURE_DAY);

        when(deskRepository.findByIdAndActiveTrue(DESK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.create(USER_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Desk not found or inactive");
    }

    @Test
    void create_userAlreadyHasReservationThatDay_throwsIllegalState() {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setDeskId(DESK_ID);
        request.setDay(FUTURE_DAY);

        when(deskRepository.findByIdAndActiveTrue(DESK_ID)).thenReturn(Optional.of(desk));
        when(reservationRepository.existsByUserIdAndDayAndStatus(USER_ID, FUTURE_DAY, ReservationStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.create(USER_ID, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already have an active reservation");
    }

    @Test
    void create_deskAlreadyBookedThatDay_throwsIllegalState() {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setDeskId(DESK_ID);
        request.setDay(FUTURE_DAY);

        when(deskRepository.findByIdAndActiveTrue(DESK_ID)).thenReturn(Optional.of(desk));
        when(reservationRepository.existsByUserIdAndDayAndStatus(USER_ID, FUTURE_DAY, ReservationStatus.ACTIVE)).thenReturn(false);
        when(reservationRepository.existsByDeskIdAndDayAndStatus(DESK_ID, FUTURE_DAY, ReservationStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.create(USER_ID, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already reserved for that day");
    }

    // -------------------------------------------------------------------------
    // cancel()
    // -------------------------------------------------------------------------

    @Test
    void cancel_happyPath_returnsCancelledResponse() {
        Reservation reservation = buildReservation(5L, user, desk, FUTURE_DAY, ReservationStatus.ACTIVE);

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CancelReservationRequest cancelRequest = new CancelReservationRequest();
        cancelRequest.setReason("Change of plans");

        ReservationResponse response = reservationService.cancel(USER_ID, 5L, cancelRequest);

        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancel_reservationNotFound_throwsIllegalArgument() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancel(USER_ID, 99L, new CancelReservationRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reservation not found");
    }

    @Test
    void cancel_notOwner_throwsSecurityException() {
        User otherUser = new User();
        otherUser.setId(999L);
        Reservation reservation = buildReservation(5L, otherUser, desk, FUTURE_DAY, ReservationStatus.ACTIVE);

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(USER_ID, 5L, new CancelReservationRequest()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("not allowed to cancel");
    }

    @Test
    void cancel_alreadyCancelled_throwsIllegalState() {
        Reservation reservation = buildReservation(5L, user, desk, FUTURE_DAY, ReservationStatus.CANCELLED);

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(USER_ID, 5L, new CancelReservationRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void cancel_pastReservation_throwsIllegalState() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Reservation reservation = buildReservation(5L, user, desk, yesterday, ReservationStatus.ACTIVE);

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(USER_ID, 5L, new CancelReservationRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only future reservations");
    }

    @Test
    void cancel_todayReservation_throwsIllegalState() {
        LocalDate today = LocalDate.now();
        Reservation reservation = buildReservation(5L, user, desk, today, ReservationStatus.ACTIVE);

        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancel(USER_ID, 5L, new CancelReservationRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only future reservations");
    }

    // -------------------------------------------------------------------------
    // getMyReservations()
    // -------------------------------------------------------------------------

    @Test
    void getMyReservations_returnsListMappedCorrectly() {
        Reservation r1 = buildReservation(1L, user, desk, FUTURE_DAY, ReservationStatus.ACTIVE);
        Reservation r2 = buildReservation(2L, user, desk, FUTURE_DAY.plusDays(1), ReservationStatus.CANCELLED);

        when(reservationRepository.findByUserIdOrderByDayDesc(USER_ID)).thenReturn(List.of(r1, r2));

        List<ReservationResponse> result = reservationService.getMyReservations(USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(result.get(1).getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void getMyReservations_noReservations_returnsEmptyList() {
        when(reservationRepository.findByUserIdOrderByDayDesc(USER_ID)).thenReturn(List.of());

        List<ReservationResponse> result = reservationService.getMyReservations(USER_ID);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private Reservation buildReservation(Long id, User owner, Desk desk, LocalDate day, ReservationStatus status) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setUser(owner);
        r.setDesk(desk);
        r.setDay(day);
        r.setStatus(status);
        // simulate @PrePersist
        try {
            var field = Reservation.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(r, LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return r;
    }
}
