package com.osotnikov.clockserver.subscription.controller.dto;

import com.osotnikov.clockserver.subscription.controller.validation.PostbackFrequencyConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDto {
    //@Pattern(regexp = "")
    private String postbackUrl;
    @PostbackFrequencyConstraint
    private FrequencyDto frequency;
}
