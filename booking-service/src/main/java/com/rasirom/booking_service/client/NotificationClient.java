package com.rasirom.booking_service.client;

import com.rasirom.booking_service.dto.NotificationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class NotificationClient {

    private final RestClient restClient;

    public NotificationClient(@Value("${services.notification-url}") String notificationUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(notificationUrl)
                .build();
    }

    public List<NotificationResponse> getNotificationsForUser(Long userId) {
        return restClient.get()
                .uri("/notifications/user/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
