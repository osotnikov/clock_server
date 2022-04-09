package com.osotnikov.clockserver.subscription.service;

import com.osotnikov.clockserver.subscription.api.dto.request.FrequencyDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionPatchDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {
	@Mock
	TaskScheduler taskScheduler;
	@InjectMocks
	SubscriptionService subscriptionService;

	@BeforeEach
	void setUp() {

	}

	@Test
	void testSchedule() {
		boolean result = subscriptionService.schedule(new SubscriptionDto("postbackUrl",
			new FrequencyDto(1, 2, 3)));
		Assertions.assertEquals(true, result);
	}

	@Test
	void testDelete() {
		boolean result = subscriptionService.delete("name");
		Assertions.assertEquals(true, result);
	}

	@Test
	void testChangeSchedule() {
		boolean result = subscriptionService.changeSchedule(new SubscriptionPatchDto("name",
			new FrequencyDto(0, 0, 0)));
		Assertions.assertEquals(true, result);
	}
}