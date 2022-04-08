package com.osotnikov.clockserver.subscription.api.dto.request;

import com.osotnikov.clockserver.subscription.api.validation.PostbackFrequencyConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPatchDto {
    @URL(regexp = "^(http|https).*")
    private String name;
    @PostbackFrequencyConstraint
    private FrequencyDto frequency;
}
