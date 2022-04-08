package com.osotnikov.clockserver.subscription.service;


import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionPatchDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

/*
* Ideally we'd want to separate the scheduling/triggering of the tasks and the management of the worker threads that
* execute them (either locally or remotely on other nodes).
* Currently, the decision is made not to have two separate services but just one since both the scheduling/triggering and
* execution will be managed by spring's TaskScheduler.
* If there will be a need in the future to use some different executor logic TaskScheduler can be extensively configured
* by using SchedulingConfigurer @Configuration component.
 * */
@Service
public class SubscriptionService {

	private final TaskScheduler taskScheduler;

	@Autowired
	public SubscriptionService(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	/**
	 * @returns: false if there is already a schedule for this subscription request, true if it is successfully created
	 * */
	public boolean schedule(SubscriptionDto subscriptionDto) {
		return false;
	}

	/**
	 * @returns: false if there was no subscription for the given name, true if it is successfully deleted
	 * */
	public boolean delete(String name) {
		return false;
	}

	/**
	 * @returns: false if there was no subscription for the given name, true if it is successfully changed
	 * */
	public boolean changeSchedule(SubscriptionPatchDto subscriptionDto) {
		return false;
	}

}
