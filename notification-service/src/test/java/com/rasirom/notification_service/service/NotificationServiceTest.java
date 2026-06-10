package com.rasirom.notification_service.service;

import com.rasirom.notification_service.event.ReservationEvent;
import com.rasirom.notification_service.model.Notification;
import com.rasirom.notification_service.model.ReservationEventType;
import com.rasirom.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private ReservationEvent createdEvent;
    private ReservationEvent cancelledEvent;

    @BeforeEach
    void setUp() {
        createdEvent = buildEvent(1L, 10L, 20L, ReservationEventType.CREATED);
        cancelledEvent = buildEvent(2L, 10L, 20L, ReservationEventType.CANCELLED);
    }

    // -------------------------------------------------------------------------
    // processReservationEvent() — happy paths
    // -------------------------------------------------------------------------

    @Test
    void process_createdEvent_savesNotificationWithCorrectFields() {
        when(notificationRepository.existsByReservationIdAndEventType(1L, ReservationEventType.CREATED))
                .thenReturn(false);

        notificationService.processReservationEvent(createdEvent);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getReservationId()).isEqualTo(1L);
        assertThat(saved.getEventType()).isEqualTo(ReservationEventType.CREATED);
        assertThat(saved.getUserId()).isEqualTo(10L);
        assertThat(saved.getDeskId()).isEqualTo(20L);
        assertThat(saved.getDeskNumber()).isEqualTo(5);
        assertThat(saved.getRoomNumber()).isEqualTo("B2");
        assertThat(saved.getFloor()).isEqualTo(3);
        assertThat(saved.getDay()).isEqualTo(LocalDate.of(2026, 7, 1));
    }

    @Test
    void process_cancelledEvent_savesNotificationWithCancelledType() {
        when(notificationRepository.existsByReservationIdAndEventType(2L, ReservationEventType.CANCELLED))
                .thenReturn(false);

        notificationService.processReservationEvent(cancelledEvent);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(ReservationEventType.CANCELLED);
    }

    // -------------------------------------------------------------------------
    // processReservationEvent() — idempotency (duplicate detection)
    // -------------------------------------------------------------------------

    @Test
    void process_duplicateEvent_checkedFirst_skipsWithoutSaving() {
        when(notificationRepository.existsByReservationIdAndEventType(1L, ReservationEventType.CREATED))
                .thenReturn(true);

        notificationService.processReservationEvent(createdEvent);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void process_duplicateDetectedByUniqueConstraint_doesNotPropagateException() {
        when(notificationRepository.existsByReservationIdAndEventType(1L, ReservationEventType.CREATED))
                .thenReturn(false);
        when(notificationRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        // must not throw — the service swallows this exception intentionally
        notificationService.processReservationEvent(createdEvent);
    }

    @Test
    void process_createdAndCancelledForSameReservation_savesEachEventOnce() {
        when(notificationRepository.existsByReservationIdAndEventType(1L, ReservationEventType.CREATED))
                .thenReturn(false);
        when(notificationRepository.existsByReservationIdAndEventType(1L, ReservationEventType.CANCELLED))
                .thenReturn(false);

        ReservationEvent cancelled = buildEvent(1L, 10L, 20L, ReservationEventType.CANCELLED);

        notificationService.processReservationEvent(createdEvent);
        notificationService.processReservationEvent(cancelled);

        verify(notificationRepository, times(2)).save(any());
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private ReservationEvent buildEvent(Long reservationId, Long userId, Long deskId, ReservationEventType type) {
        ReservationEvent event = new ReservationEvent();
        event.setReservationId(reservationId);
        event.setUserId(userId);
        event.setDeskId(deskId);
        event.setEventType(type);
        event.setDeskNumber(5);
        event.setRoomNumber("B2");
        event.setFloor(3);
        event.setDay(LocalDate.of(2026, 7, 1));
        return event;
    }
}
