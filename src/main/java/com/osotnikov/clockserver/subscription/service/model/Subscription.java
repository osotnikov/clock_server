package com.osotnikov.clockserver.subscription.service.model;

import com.osotnikov.clockserver.subscription.api.dto.request.FrequencyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ScheduledFuture;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
	private String postbackUrl;
	private FrequencyDto frequency;
	private ScheduledFuture scheduledFuture;
}
