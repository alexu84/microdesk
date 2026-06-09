package com.rasirom.notification_service.service;

import com.rasirom.notification_service.event.ReservationEvent;
import com.rasirom.notification_service.model.Notification;
import com.rasirom.notification_service.model.ReservationEventType;
import com.rasirom.notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void processReservationEvent(ReservationEvent event) {
        ReservationEventType eventType = event.getEventType();

        if (notificationRepository.existsByReservationIdAndEventType(event.getReservationId(), eventType)) {
            log.info("Duplicate event ignored for reservationId={}, type={}", event.getReservationId(), eventType);
            return;
        }

        Notification notification = new Notification();
        notification.setReservationId(event.getReservationId());
        notification.setEventType(eventType);
        notification.setUserId(event.getUserId());
        notification.setDeskId(event.getDeskId());
        notification.setDeskNumber(event.getDeskNumber());
        notification.setRoomNumber(event.getRoomNumber());
        notification.setFloor(event.getFloor());
        notification.setDay(event.getDay());

        try {
            notificationRepository.save(notification);
            log.info("Event saved: reservationId={}, type={}, userId={}",
                    event.getReservationId(), eventType, event.getUserId());
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate event caught by unique constraint for reservationId={}, type={}",
                    event.getReservationId(), eventType);
        }
    }
}
