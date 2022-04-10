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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

	private static final String URL = "http://some.url/path";

	@Mock
	private SubscriptionDto subscriptionDto;
	@Mock
	private SubscriptionPatchDto subscriptionPatchDto;
	@Mock
	private ScheduledFuture scheduledFuture;

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

	@Test
	void givenSubscriptionExists_whenSchedule_thenReturnFalse() {
		Subscription subscription = mock(Subscription.class);
		when(subscriptionDtoMapper.map(subscriptionDto)).thenReturn(subscription);
		when(subscriptionRepository.storeNew(subscription)).thenReturn(mock(Subscription.class));

		assertFalse(subscriptionService.schedule(subscriptionDto));
	}

	@Test
	void givenSubscriptionDoesNotExist_whenSchedule_thenScheduleAndReturnTrue() {
		FrequencyDto frequencyDto = new FrequencyDto(1, 2, 3);
		Subscription subscription = new Subscription(URL, frequencyDto, null);
		when(subscriptionDtoMapper.map(subscriptionDto)).thenReturn(subscription);
		when(subscriptionRepository.storeNew(subscription)).thenReturn(null);
		TimePostbackRunnable timePostbackRunnable = mock(TimePostbackRunnable.class);
		when(timePostbackRunnableFactory.createRunnable(URL)).thenReturn(timePostbackRunnable);
		ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
		when(taskScheduler.scheduleAtFixedRate(eq(timePostbackRunnable), durationCaptor.capture()))
			.thenReturn(scheduledFuture);

		assertTrue(subscriptionService.schedule(subscriptionDto));

		assertEquals(scheduledFuture, subscription.getScheduledFuture());
		Duration actualDuration = durationCaptor.getValue();
		assertEquals(3723, actualDuration.getSeconds());
		assertEquals(scheduledFuture, subscription.getScheduledFuture());
	}

	@Test
	void givenSubscriptionDoesNotExist_whenDelete_thenReturnFalse() {
		when(subscriptionRepository.get(URL)).thenReturn(null);

		assertFalse(subscriptionService.delete(URL));

		then(subscriptionRepository).shouldHaveNoMoreInteractions();
	}

	@Test
	void givenSubscriptionExistsAndIsNotChangedDuringExecution_whenDelete_thenReturnTrue() {
		Subscription subscription = mock(Subscription.class);
		when(subscription.getScheduledFuture()).thenReturn(mock(ScheduledFuture.class));
		when(subscriptionRepository.get(URL)).thenReturn(subscription);

		assertTrue(subscriptionService.delete(URL));

		then(subscriptionRepository).should().deleteExisting(URL);
		then(subscriptionRepository).shouldHaveNoMoreInteractions();
	}

	@Test
	void givenSubscriptionExistsAndIsNotChangedDuringExecutionButIsNotScheduled_whenDelete_thenReturnFalse() {
		Subscription subscription = mock(Subscription.class);
		when(subscriptionRepository.get(URL)).thenReturn(subscription);

		assertFalse(subscriptionService.delete(URL));

		then(subscriptionRepository).shouldHaveNoMoreInteractions();
	}

	@Test
	void givenSubscriptionExistsAndIsChangedDuringExecution_whenDelete_thenReturnFalse() {
		Subscription subscription = mock(Subscription.class);
		Subscription subscription2 = mock(Subscription.class);
		when(subscriptionRepository.get(URL)).thenReturn(subscription, subscription2);

		assertFalse(subscriptionService.delete(URL));

		then(subscriptionRepository).shouldHaveNoMoreInteractions();
	}

	@Test
	void givenSubscriptionExistsAndIsNotChangedDuringExecution_whenChangeSchedule_thenTrueAndRescheduled() {
		FrequencyDto frequencyDto = new FrequencyDto(0, 2, 3);
		SubscriptionPatchDto subscriptionPatchDto = new SubscriptionPatchDto(URL, frequencyDto);
		FrequencyDto frequencyDto2 = new FrequencyDto(1, 2, 3);
		Subscription subscription = new Subscription(URL, frequencyDto2, scheduledFuture);
		when(subscriptionRepository.get(URL)).thenReturn(subscription);
		TimePostbackRunnable timePostbackRunnable = mock(TimePostbackRunnable.class);
		when(timePostbackRunnableFactory.createRunnable(URL)).thenReturn(timePostbackRunnable);
		ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
		ScheduledFuture scheduledFuture2 = mock(ScheduledFuture.class);
		when(taskScheduler.scheduleAtFixedRate(eq(timePostbackRunnable), durationCaptor.capture()))
			.thenReturn(scheduledFuture2);

		assertTrue(subscriptionService.changeSchedule(subscriptionPatchDto));

		Duration actualDuration = durationCaptor.getValue();
		assertEquals(123, actualDuration.getSeconds());
		assertEquals(scheduledFuture2, subscription.getScheduledFuture());
		then(scheduledFuture).should().cancel(false);
		then(subscriptionRepository).shouldHaveNoMoreInteractions();
	}

	@Test
	void givenSubscriptionExistsAndIsChangedDuringExecution_whenChangeSchedule_thenFalse() {
		FrequencyDto frequencyDto = new FrequencyDto(0, 2, 3);
		SubscriptionPatchDto subscriptionPatchDto = new SubscriptionPatchDto(URL, frequencyDto);
		FrequencyDto frequencyDto2 = new FrequencyDto(1, 2, 3);
		Subscription subscription = new Subscription(URL, frequencyDto2, scheduledFuture);
		when(subscriptionRepository.get(URL)).thenReturn(subscription, mock(Subscription.class));

		assertFalse(subscriptionService.changeSchedule(subscriptionPatchDto));
		then(subscriptionRepository).shouldHaveNoMoreInteractions();
	}

	@Test
	void givenSubscriptionExistsAndIsNotYetScheduled_whenChangeSchedule_thenFalse() {
		FrequencyDto frequencyDto = new FrequencyDto(1, 2, 3);
		SubscriptionPatchDto subscriptionPatchDto = new SubscriptionPatchDto(URL, frequencyDto);
		Subscription subscription = new Subscription(URL, frequencyDto, null);
		when(subscriptionRepository.get(URL)).thenReturn(subscription);

		assertFalse(subscriptionService.changeSchedule(subscriptionPatchDto));
		then(subscriptionRepository).shouldHaveNoMoreInteractions();
	}
}