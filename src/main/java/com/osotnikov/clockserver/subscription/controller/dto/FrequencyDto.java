package com.osotnikov.clockserver.subscription.controller.dto;

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
