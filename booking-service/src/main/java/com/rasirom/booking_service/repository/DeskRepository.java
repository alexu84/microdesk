package com.rasirom.booking_service.repository;

import com.rasirom.booking_service.model.Desk;
import com.rasirom.booking_service.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeskRepository extends JpaRepository<Desk, Long> {

    Optional<Desk> findByIdAndActiveTrue(Long id);

    @Query("SELECT d FROM Desk d WHERE d.active = true AND d.id NOT IN " +
           "(SELECT r.desk.id FROM Reservation r WHERE r.day = :day AND r.status = :status)")
    List<Desk> findAvailableForDay(@Param("day") LocalDate day,
                                   @Param("status") ReservationStatus status);
}
