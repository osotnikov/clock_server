package com.osotnikov.clockserver.subscription.service;


import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionPatchDto;
import com.osotnikov.clockserver.subscription.repository.SubscriptionRepository;
import com.osotnikov.clockserver.subscription.service.mapper.SubscriptionDtoMapper;
import com.osotnikov.clockserver.subscription.service.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

/*
* Ideally we'd want to separate the scheduling/triggering of the tasks and the management of the worker threads that
* execute them (either locally or remotely on other nodes).
* Currently, the decision is made not to have two separate services for this since both the scheduling/triggering and
* execution will be managed by spring's TaskScheduler.
* If there will be a need in the future to use some different executor logic TaskScheduler can be extensively configured
* by using SchedulingConfigurer @Configuration component.
* If there will be a need not to use TaskScheduler, we could create our own scheduler and executor and provide them
* as dependencies to this class which would change the implementation but not its API.
*
* As far as the concurrency is concerned care was taken to ensure that there will never be two concurrent schedules
* for the same subscription, or a schedule for a deleted subscription. There are no other guarantees given for scenarios
* with multiple different concurrent requests for the same subscription.
 * */
@Service
public class SubscriptionService {

	private final SubscriptionDtoMapper subscriptionDtoMapper;
	private final SubscriptionRepository subscriptionRepository;
	private final TimePostbackRunnableFactory timePostbackRunnableFactory;
	private final TaskScheduler taskScheduler;

	@Autowired
	public SubscriptionService(SubscriptionDtoMapper subscriptionDtoMapper, SubscriptionRepository subscriptionRepository,
							   TimePostbackRunnableFactory timePostbackRunnableFactory, TaskScheduler taskScheduler) {
		this.subscriptionDtoMapper = subscriptionDtoMapper;
		this.subscriptionRepository = subscriptionRepository;
		this.timePostbackRunnableFactory = timePostbackRunnableFactory;
		this.taskScheduler = taskScheduler;
	}

	/**
	 * @returns: false if there is already a schedule for this subscription request, true if it is successfully created
	 * */
	public boolean schedule(SubscriptionDto subscriptionDto) {
		Subscription subscription = subscriptionDtoMapper.map(subscriptionDto);
		subscription = subscriptionRepository.storeNew(subscription);
		if (subscription != null) {
			return false;
		}
		// subscription might have been deleted or changed before entering this synchronized block. "Changed" in this
		// context means the instance might be the same or different, depending on whether just the schedule was changed
		// or it was deleted and recreated.
		synchronized(subscription) {
			subscription = subscriptionRepository.get(subscription.getPostbackUrl());
			if(subscription.getScheduledFuture() == null) {
				scheduleSubscription(subscription);
			}
			return true;
		}
	}

	/**
	 * @returns: false if there was no subscription for the given name, true if it is successfully deleted
	 * */
	public boolean delete(String name) {
		Subscription subscription = subscriptionRepository.get(name);
		// subscription might have been changed before entering this synchronized block, that means scheduled feature
		// reference might be stale
		synchronized(subscription) {
			// most recent subscription instance is returned though so reference to scheduled feature is correct
			subscription = subscriptionRepository.deleteExisting(name);
			if (subscription != null) {
				cancelSubscription(subscription);
				return true;
			}
			return false;
		}
	}

	/**
	 * @returns: false if there was no subscription for the given name, true if it is successfully changed
	 * */
	public boolean changeSchedule(SubscriptionPatchDto subscriptionPatchDto) {
		Subscription subscription = subscriptionRepository.get(subscriptionPatchDto.getName());
		// subscription might have been deleted or changed before entering this synchronized block
		synchronized (subscription) {
			// if it was deleted we need to exit, if it's been changed we need the reference to the new scheduled feature
			subscription = subscriptionRepository.get(subscriptionPatchDto.getName());
			if(subscription == null) {
				return false;
			}
			cancelSubscription(subscription);
			subscription.setFrequency(subscriptionPatchDto.getFrequency());
			scheduleSubscription(subscription);
			return true;
		}
	}

	private void cancelSubscription(Subscription subscription) {
		if(subscription.getScheduledFuture() != null) {
			subscription.getScheduledFuture().cancel(false);
		}
	}

	private void scheduleSubscription(Subscription subscription) {
		Duration duration = Duration.ofSeconds(subscription.getFrequency().getSeconds())
			.plusMinutes(subscription.getFrequency().getMinutes())
			.plusHours(subscription.getFrequency().getHours());
		ScheduledFuture sf = taskScheduler.scheduleAtFixedRate(
			timePostbackRunnableFactory.createRunnable(subscription.getPostbackUrl()),
			duration);
		subscription.setScheduledFuture(sf);
	}

}
