package com.rasirom.booking_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasirom.booking_service.event.ReservationEvent;
import com.rasirom.booking_service.model.OutboxMessage;
import com.rasirom.booking_service.model.OutboxStatus;
import com.rasirom.booking_service.repository.OutboxMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxRelayService {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayService.class);

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, ReservationEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String reservationsTopic;

    public OutboxRelayService(OutboxMessageRepository outboxMessageRepository,
                              KafkaTemplate<String, ReservationEvent> kafkaTemplate,
                              ObjectMapper objectMapper,
                              @Value("${kafka.topics.reservations}") String reservationsTopic) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.reservationsTopic = reservationsTopic;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay-ms}")
    @Transactional
    public void relay() {
        List<OutboxMessage> pending =
                outboxMessageRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxMessage message : pending) {
            try {
                ReservationEvent event = objectMapper.readValue(message.getPayload(), ReservationEvent.class);

                kafkaTemplate.send(reservationsTopic, String.valueOf(event.getReservationId()), event)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send outbox message id={} to Kafka: {}", message.getId(), ex.getMessage());
                            }
                        })
                        .get(); // block until Kafka ACK

                message.setStatus(OutboxStatus.SENT);
                message.setProcessedAt(LocalDateTime.now());
                outboxMessageRepository.save(message);

            } catch (Exception ex) {
                log.error("Error processing outbox message id={}: {}", message.getId(), ex.getMessage());
            }
        }
    }
}
