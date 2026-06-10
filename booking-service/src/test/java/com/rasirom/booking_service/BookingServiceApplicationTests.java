package com.rasirom.booking_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
class BookingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
