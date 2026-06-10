package com.rasirom.booking_service.client;

import com.rasirom.booking_service.dto.NotificationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);
    private static final String NOTIFICATION_SERVICE = "notificationService";

    private final RestClient restClient;

    public NotificationClient(@Value("${services.notification-url}") String notificationUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(notificationUrl)
                .build();
    }

    @CircuitBreaker(name = NOTIFICATION_SERVICE, fallbackMethod = "getNotificationsForUserFallback")
    @Retry(name = NOTIFICATION_SERVICE)
    public List<NotificationResponse> getNotificationsForUser(Long userId) {
        return restClient.get()
                .uri("/notifications/user/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private List<NotificationResponse> getNotificationsForUserFallback(Long userId, Throwable t) {
        log.warn("Notification service unavailable for userId={}, returning empty list. Reason: {}", userId, t.getMessage());
        return Collections.emptyList();
    }
}
