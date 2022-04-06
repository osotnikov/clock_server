package com.osotnikov.clockserver.subscription.controller.dto;

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
    // min 5s max 4h
    private FrequencyDto frequency;
}
