package com.rasirom.booking_service.dto;

import com.rasirom.booking_service.model.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;
    private Long userId;
    private Long deskId;
    private Integer deskNumber;
    private String roomNumber;
    private Integer floor;
    private LocalDate day;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    public ReservationResponse(Long id, Long userId, Long deskId, Integer deskNumber,
                               String roomNumber, Integer floor, LocalDate day,
                               ReservationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.deskId = deskId;
        this.deskNumber = deskNumber;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.day = day;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getDeskId() { return deskId; }
    public Integer getDeskNumber() { return deskNumber; }
    public String getRoomNumber() { return roomNumber; }
    public Integer getFloor() { return floor; }
    public LocalDate getDay() { return day; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
