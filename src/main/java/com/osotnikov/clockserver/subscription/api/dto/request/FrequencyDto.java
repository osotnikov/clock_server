package com.osotnikov.clockserver.subscription.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrequencyDto {
	private int hours;
	private int minutes;
	private int seconds;
}
