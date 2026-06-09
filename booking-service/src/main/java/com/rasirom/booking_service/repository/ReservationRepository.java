package com.rasirom.booking_service.repository;

import com.rasirom.booking_service.model.Reservation;
import com.rasirom.booking_service.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByDeskIdAndDayAndStatus(Long deskId, LocalDate day, ReservationStatus status);

    List<Reservation> findByUserIdOrderByDayDesc(Long userId);
}
