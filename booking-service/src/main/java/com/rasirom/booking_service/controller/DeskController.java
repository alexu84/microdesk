package com.rasirom.booking_service.controller;

import com.rasirom.booking_service.dto.DeskResponse;
import com.rasirom.booking_service.service.DeskService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/desks")
public class DeskController {

    private final DeskService deskService;

    public DeskController(DeskService deskService) {
        this.deskService = deskService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<DeskResponse>> getAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        return ResponseEntity.ok(deskService.getAvailableDesks(day));
    }
}
