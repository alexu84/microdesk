package com.rasirom.booking_service.repository;

import com.rasirom.booking_service.model.Desk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeskRepository extends JpaRepository<Desk, Long> {

    Optional<Desk> findByIdAndActiveTrue(Long id);
}
