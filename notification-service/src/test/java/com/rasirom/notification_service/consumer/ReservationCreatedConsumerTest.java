package com.rasirom.notification_service.consumer;

import com.rasirom.notification_service.event.ReservationEvent;
import com.rasirom.notification_service.model.ReservationEventType;
import com.rasirom.notification_service.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationCreatedConsumerTest {

    @Mock private NotificationService notificationService;
    @Mock private Acknowledgment ack;

    @InjectMocks
    private ReservationCreatedConsumer consumer;

    @Test
    void consume_successfulProcessing_acknowledgesMessage() {
        ReservationEvent event = buildEvent(ReservationEventType.CREATED);
        ConsumerRecord<String, ReservationEvent> record =
                new ConsumerRecord<>("reservations", 0, 0L, "key-1", event);

        consumer.consume(record, ack);

        verify(notificationService).processReservationEvent(event);
        verify(ack).acknowledge();
    }

    @Test
    void consume_serviceThrowsException_doesNotAcknowledge() {
        ReservationEvent event = buildEvent(ReservationEventType.CREATED);
        ConsumerRecord<String, ReservationEvent> record =
                new ConsumerRecord<>("reservations", 0, 1L, "key-2", event);

        doThrow(new RuntimeException("processing failed"))
                .when(notificationService).processReservationEvent(any());

        consumer.consume(record, ack);

        verify(ack, never()).acknowledge();
    }

    @Test
    void consume_nullEventValue_doesNotAcknowledge() {
        ConsumerRecord<String, ReservationEvent> record =
                new ConsumerRecord<>("reservations", 0, 2L, "key-3", null);

        doThrow(new NullPointerException())
                .when(notificationService).processReservationEvent(null);

        consumer.consume(record, ack);

        verify(ack, never()).acknowledge();
    }

    private ReservationEvent buildEvent(ReservationEventType type) {
        ReservationEvent event = new ReservationEvent();
        event.setReservationId(1L);
        event.setUserId(10L);
        event.setDeskId(20L);
        event.setEventType(type);
        return event;
    }
}
