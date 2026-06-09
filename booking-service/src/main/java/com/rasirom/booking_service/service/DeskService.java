package com.rasirom.booking_service.service;

import com.rasirom.booking_service.dto.DeskResponse;
import com.rasirom.booking_service.model.ReservationStatus;
import com.rasirom.booking_service.repository.DeskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeskService {

    private final DeskRepository deskRepository;

    public DeskService(DeskRepository deskRepository) {
        this.deskRepository = deskRepository;
    }

    public List<DeskResponse> getAvailableDesks(LocalDate day) {
        return deskRepository.findAvailableForDay(day, ReservationStatus.ACTIVE).stream()
                .map(d -> new DeskResponse(d.getId(), d.getDeskNumber(), d.getRoomNumber(),
                        d.getFloor(), d.getDescription()))
                .collect(Collectors.toList());
    }
}
