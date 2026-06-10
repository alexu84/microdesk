package com.rasirom.notification_service.dto;

import com.rasirom.notification_service.model.ReservationEventType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private Long reservationId;
    private ReservationEventType eventType;
    private Long userId;
    private Long deskId;
    private Integer deskNumber;
    private String roomNumber;
    private Integer floor;
    private LocalDate day;
    private LocalDateTime processedAt;

    public NotificationResponse(Long id, Long reservationId, ReservationEventType eventType,
                                Long userId, Long deskId, Integer deskNumber, String roomNumber,
                                Integer floor, LocalDate day, LocalDateTime processedAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.eventType = eventType;
        this.userId = userId;
        this.deskId = deskId;
        this.deskNumber = deskNumber;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.day = day;
        this.processedAt = processedAt;
    }

    public Long getId() { return id; }
    public Long getReservationId() { return reservationId; }
    public ReservationEventType getEventType() { return eventType; }
    public Long getUserId() { return userId; }
    public Long getDeskId() { return deskId; }
    public Integer getDeskNumber() { return deskNumber; }
    public String getRoomNumber() { return roomNumber; }
    public Integer getFloor() { return floor; }
    public LocalDate getDay() { return day; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}
