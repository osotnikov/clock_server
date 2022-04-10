package com.osotnikov.clockserver.subscription.service;

import com.osotnikov.clockserver.subscription.service.model.rest.client.dto.request.CurrentTimeDto;
import com.osotnikov.rest.client.RestClient;

import java.util.Date;

public class TimePostbackRunnable implements Runnable {

	private final String postbackUrl;
	private final RestClient<CurrentTimeDto> restClient;

	TimePostbackRunnable(String postbackUrl, RestClient<CurrentTimeDto> restClient) {
		this.postbackUrl = postbackUrl;
		this.restClient = restClient;
	}

	@Override
	public void run() {
		restClient.postAndIgnoreResponseBody(postbackUrl, new CurrentTimeDto(new Date().toString()));
	}
}
