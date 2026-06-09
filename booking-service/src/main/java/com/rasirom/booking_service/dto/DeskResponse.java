package com.rasirom.booking_service.dto;

public class DeskResponse {

    private Long id;
    private Integer deskNumber;
    private String roomNumber;
    private Integer floor;
    private String description;

    public DeskResponse(Long id, Integer deskNumber, String roomNumber, Integer floor, String description) {
        this.id = id;
        this.deskNumber = deskNumber;
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.description = description;
    }

    public Long getId() { return id; }
    public Integer getDeskNumber() { return deskNumber; }
    public String getRoomNumber() { return roomNumber; }
    public Integer getFloor() { return floor; }
    public String getDescription() { return description; }
}
