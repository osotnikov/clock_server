package com.osotnikov.clockserver.subscription.controller.dto;

import com.osotnikov.clockserver.subscription.controller.validation.PostbackFrequencyConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDto {
    @URL(regexp = "^(http|https).*")
    private String postbackUrl;
    @PostbackFrequencyConstraint
    private FrequencyDto frequency;
}
