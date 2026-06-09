package com.rasirom.notification_service.repository;

import com.rasirom.notification_service.model.Notification;
import com.rasirom.notification_service.model.ReservationEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByReservationIdAndEventType(Long reservationId, ReservationEventType eventType);
}
