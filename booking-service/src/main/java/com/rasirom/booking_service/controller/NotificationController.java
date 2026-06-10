package com.rasirom.booking_service.controller;

import com.rasirom.booking_service.client.NotificationClient;
import com.rasirom.booking_service.dto.NotificationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationClient notificationClient;

    public NotificationController(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Principal principal) {
        Long userId = (Long) ((UsernamePasswordAuthenticationToken) principal).getDetails();
        return ResponseEntity.ok(notificationClient.getNotificationsForUser(userId));
    }
}
