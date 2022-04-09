package com.osotnikov.clockserver.subscription.repository;

import com.osotnikov.clockserver.subscription.service.model.Subscription;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SubscriptionRepository {

	private final ConcurrentHashMap<String, Subscription> subscriptions = new ConcurrentHashMap<>();

	/**
	 * @return already existing subscription, if subscription did not exist returns null
	 * */
	public Subscription storeNew(Subscription subscription) {
		return subscriptions.putIfAbsent(subscription.getPostbackUrl(), subscription);
	}

	public Subscription deleteExisting(String name) {
		return subscriptions.remove(name);
	}

	public Subscription get(String name) {
		return subscriptions.get(name);
	}
}
