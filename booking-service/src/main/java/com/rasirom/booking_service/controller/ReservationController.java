package com.rasirom.booking_service.controller;

import com.rasirom.booking_service.dto.CreateReservationRequest;
import com.rasirom.booking_service.dto.ReservationResponse;
import com.rasirom.booking_service.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Principal principal) {
        Long userId = (Long) ((UsernamePasswordAuthenticationToken) principal).getDetails();
        return ResponseEntity.ok(reservationService.getMyReservations(userId));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateReservationRequest request,
                                    Principal principal) {
        try {
            Long userId = (Long) ((UsernamePasswordAuthenticationToken) principal).getDetails();
            ReservationResponse response = reservationService.create(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
