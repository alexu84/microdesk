package com.rasirom.booking_service.repository;

import com.rasirom.booking_service.model.OutboxMessage;
import com.rasirom.booking_service.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
