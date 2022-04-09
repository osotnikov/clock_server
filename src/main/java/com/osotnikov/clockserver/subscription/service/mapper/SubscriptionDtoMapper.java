package com.osotnikov.clockserver.subscription.service.mapper;

import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionPatchDto;
import com.osotnikov.clockserver.subscription.service.model.Subscription;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionDtoMapper {

	public Subscription map(SubscriptionDto subscriptionDto) {
		return new Subscription(subscriptionDto.getPostbackUrl(), subscriptionDto.getFrequency(), null);
	}

	public Subscription map(SubscriptionPatchDto subscriptionPatchDto) {
		return new Subscription(subscriptionPatchDto.getName(), subscriptionPatchDto.getFrequency(), null);
	}
}
