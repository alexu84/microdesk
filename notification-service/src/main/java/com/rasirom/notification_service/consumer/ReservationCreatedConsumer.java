package com.rasirom.notification_service.consumer;

import com.rasirom.notification_service.event.ReservationEvent;
import com.rasirom.notification_service.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class ReservationCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReservationCreatedConsumer.class);

    private final NotificationService notificationService;

    public ReservationCreatedConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "${kafka.topics.reservations}",
            groupId = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, ReservationEvent> record, Acknowledgment ack) {
        log.info("Received event: topic={}, partition={}, offset={}, key={}, type={}",
                record.topic(), record.partition(), record.offset(), record.key(),
                record.value() != null ? record.value().getEventType() : null);
        try {
            notificationService.processReservationEvent(record.value());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process event for key={}, offset={}. Will not acknowledge.",
                    record.key(), record.offset(), e);
        }
    }
}
