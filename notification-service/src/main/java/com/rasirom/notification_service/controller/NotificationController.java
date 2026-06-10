package com.rasirom.notification_service.controller;

import com.rasirom.notification_service.dto.NotificationResponse;
import com.rasirom.notification_service.model.Notification;
import com.rasirom.notification_service.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getByUser(@PathVariable Long userId) {
        List<NotificationResponse> response = notificationRepository
                .findByUserIdOrderByProcessedAtDesc(userId)
                .stream()
                .map(n -> new NotificationResponse(
                        n.getId(), n.getReservationId(), n.getEventType(),
                        n.getUserId(), n.getDeskId(), n.getDeskNumber(),
                        n.getRoomNumber(), n.getFloor(), n.getDay(), n.getProcessedAt()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
