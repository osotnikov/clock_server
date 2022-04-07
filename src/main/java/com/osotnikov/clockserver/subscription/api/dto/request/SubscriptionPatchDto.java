package com.osotnikov.clockserver.subscription.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * If there were more fields I'd probably just deserialize the request json to a map structure but now there's only
 * one field that can be modified, it makes more sense to just fail the patch if it is not provided.
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPatchDto {
    private FrequencyDto frequency;
}
