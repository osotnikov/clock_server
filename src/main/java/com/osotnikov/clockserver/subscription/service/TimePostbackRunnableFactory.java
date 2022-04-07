package com.osotnikov.clockserver.subscription.service;

import com.osotnikov.clockserver.subscription.service.model.rest.client.dto.request.CurrentTimeDto;
import com.osotnikov.rest.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimePostbackRunnableFactory {

	private RestClient<CurrentTimeDto> restClient = new RestClient<>();

	@Autowired
	public TimePostbackRunnableFactory() {

	}

}
