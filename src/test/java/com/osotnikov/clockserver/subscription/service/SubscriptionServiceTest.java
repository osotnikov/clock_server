package com.osotnikov.clockserver.subscription.service;

import com.osotnikov.clockserver.subscription.api.dto.request.FrequencyDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionPatchDto;
import com.osotnikov.clockserver.subscription.repository.SubscriptionRepository;
import com.osotnikov.clockserver.subscription.service.mapper.SubscriptionDtoMapper;
import com.osotnikov.clockserver.subscription.service.model.Subscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

	private static final String URL = "http://some.url/path";

	@Mock
	private SubscriptionDto subscriptionDto;
	@Mock
	ScheduledFuture scheduledFuture;

	@Mock
	private SubscriptionDtoMapper subscriptionDtoMapper;
	@Mock
	private SubscriptionRepository subscriptionRepository;
	@Mock
	private TimePostbackRunnableFactory timePostbackRunnableFactory;
	@Mock
	private TaskScheduler taskScheduler;
	@InjectMocks
	private SubscriptionService subscriptionService;

	@BeforeEach
	void setUp() {

	}

	@Test
	void testSchedule() {
		Subscription subscription = new Subscription(URL,
			new FrequencyDto(0, 0, 0), scheduledFuture);
		when(subscriptionDtoMapper.map(subscriptionDto)).thenReturn(subscription);
		when(subscriptionRepository.storeNew(subscription)).thenReturn(null);
		when(subscriptionRepository.get(URL)).thenReturn(new Subscription("postbackUrl",
			new FrequencyDto(0, 0, 0), scheduledFuture));
		when(timePostbackRunnableFactory.createRunnable(anyString())).thenReturn(new TimePostbackRunnable("postbackUrl"));

		boolean result = subscriptionService.schedule(new SubscriptionDto("postbackUrl",
			new FrequencyDto(0, 0, 0)));
		Assertions.assertEquals(true, result);
	}

	@Test
	void testDelete() {
		when(subscriptionRepository.deleteExisting(anyString())).thenReturn(new Subscription("postbackUrl",
			new FrequencyDto(0, 0, 0), scheduledFuture));
		when(subscriptionRepository.get(anyString())).thenReturn(new Subscription("postbackUrl", new
			FrequencyDto(0, 0, 0), scheduledFuture));

		boolean result = subscriptionService.delete("name");
		Assertions.assertEquals(true, result);
	}

	@Test
	void testChangeSchedule() {
		when(subscriptionRepository.get(anyString())).thenReturn(new Subscription("postbackUrl",
			new FrequencyDto(0, 0, 0), scheduledFuture));
		when(timePostbackRunnableFactory.createRunnable(anyString())).thenReturn(new TimePostbackRunnable("postbackUrl"));

		boolean result = subscriptionService.changeSchedule(new SubscriptionPatchDto("name", new FrequencyDto(0, 0, 0)));
		Assertions.assertEquals(true, result);
	}
}