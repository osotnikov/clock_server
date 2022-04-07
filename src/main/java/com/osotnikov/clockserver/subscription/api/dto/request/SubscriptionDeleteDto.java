package com.osotnikov.clockserver.subscription.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDeleteDto {
	@URL(regexp = "^(http|https).*")
	private String name;
}
