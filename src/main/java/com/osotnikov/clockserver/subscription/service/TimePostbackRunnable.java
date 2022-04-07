package com.osotnikov.clockserver.subscription.service;

public class TimePostbackRunnable implements Runnable {

	private final String postbackUrl;

	TimePostbackRunnable(String postbackUrl) {
		this.postbackUrl = postbackUrl;
	}

	@Override
	public void run() {

	}
}
