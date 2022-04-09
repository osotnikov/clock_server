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
* As far as the concurrency is concerned the decision was to synchronize on the element level instead of the store/action
* level which resulted in a more complex code. Care was taken to ensure that there will never be two concurrent schedules
* for the same subscription, or a schedule for a deleted subscription. There are no other guarantees given for scenarios
* with multiple different concurrent requests for the same subscription.
 * */
@Service
public class SubscriptionService {

	private final SubscriptionDtoMapper subscriptionDtoMapper;
	private final SubscriptionRepository subscriptionRepository; // any method calls in this service are atomic
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
		subscription = subscriptionRepository.storeNew(subscription); // atomic
		if (subscription != null) {
			return false; // subscription already exists
		}
		// subscription has now been created with subscription.getScheduledFuture() == null, any deletes or patches
		// that might have happened immediately after will not go through and any other creates will return from
		// the "if" above (see delete and changeSchedule methods), therefore there is not need for a synchronized block
		// here.
		scheduleSubscription(subscription);
		return true;
	}

	/**
	 * @returns: false if there was no subscription for the given name, true if it is successfully deleted
	 * */
	public boolean delete(String name) {
		Subscription subscription = subscriptionRepository.get(name);
		if(subscription == null) {
			return false;
		}
		// subscription might have been changed before entering this synchronized block
		synchronized(subscription) {
			Subscription subscription2 = subscriptionRepository.get(name);
			if(subscription != subscription2 || subscription.getScheduledFuture() == null) {
				// If subscription.getScheduledFuture() has not been initialized don't consider this as an existing subscription
				// since it is in the process of initialization.
				return false;
			}
			subscriptionRepository.deleteExisting(name);
			subscription.getScheduledFuture().cancel(false);
			return true;
		}
	}

	/**
	 * @returns: false if there was no subscription for the given name, true if it is successfully changed
	 * */
	public boolean changeSchedule(SubscriptionPatchDto subscriptionPatchDto) {
		Subscription subscription = subscriptionRepository.get(subscriptionPatchDto.getName());
		if(subscription == null) {
			return false;
		}
		// subscription might have been deleted or changed before entering this synchronized block, also it might not
		// have been fully initialized.
		synchronized (subscription) {
			if(subscription.getScheduledFuture() == null) {
				// If subscription.getScheduledFuture() has not been initialized don't consider this as an existing
				// subscription since it is in the process of initialization.
				return false;
			}
			// if it was deleted or is just being created we need to exit, if it's been changed we need the reference
			// to the new scheduled feature
			Subscription subscription2 = subscriptionRepository.get(subscriptionPatchDto.getName());
			if(subscription2 != subscription || subscription == null || subscription.getScheduledFuture() == null) {
				return false;
			}
			subscription.getScheduledFuture().cancel(false);
			subscription.setFrequency(subscriptionPatchDto.getFrequency());
			scheduleSubscription(subscription);
			return true;
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
