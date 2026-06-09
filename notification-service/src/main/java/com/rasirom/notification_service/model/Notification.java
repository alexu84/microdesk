package com.rasirom.notification_service.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_events", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"reservation_id", "event_type"})
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private ReservationEventType eventType;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long deskId;

    @Column(nullable = false)
    private Integer deskNumber;

    @Column(nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private LocalDate day;

    @Column(nullable = false, updatable = false)
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        processedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public ReservationEventType getEventType() { return eventType; }
    public void setEventType(ReservationEventType eventType) { this.eventType = eventType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getDeskId() { return deskId; }
    public void setDeskId(Long deskId) { this.deskId = deskId; }

    public Integer getDeskNumber() { return deskNumber; }
    public void setDeskNumber(Integer deskNumber) { this.deskNumber = deskNumber; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }

    public LocalDateTime getProcessedAt() { return processedAt; }
}
