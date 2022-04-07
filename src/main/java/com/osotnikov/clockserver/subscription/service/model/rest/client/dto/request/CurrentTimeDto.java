package com.osotnikov.clockserver.subscription.service.model.rest.client.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentTimeDto {
	private String currentTime;
}
