package com.rasirom.booking_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateReservationRequest {

    @NotNull(message = "Desk ID is required")
    private Long deskId;

    @NotNull(message = "Day is required")
    @Future(message = "Day must be in the future")
    private LocalDate day;

    public Long getDeskId() { return deskId; }
    public void setDeskId(Long deskId) { this.deskId = deskId; }

    public LocalDate getDay() { return day; }
    public void setDay(LocalDate day) { this.day = day; }
}
