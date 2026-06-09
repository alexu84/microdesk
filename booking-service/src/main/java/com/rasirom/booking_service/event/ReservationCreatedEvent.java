package com.rasirom.booking_service.event;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationCreatedEvent {

    private Long reservationId;
    private Long userId;
    private Long deskId;
    private Integer deskNumber;
    private String roomNumber;
    private Integer floor;
    private LocalDate day;
    private LocalDateTime createdAt;

    public ReservationCreatedEvent() {}

    public ReservationCreatedEvent(Long reservationId, Long userId, Long deskId,
                                   Integer deskNumber, String roomNumber, Integer floor,
                                   LocalDate day, LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.deskId = deskId;
        this.deskNumber = deskNumber;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.day = day;
        this.createdAt = createdAt;
    }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
