package com.osotnikov.clockserver.subscription.api;

import com.osotnikov.clockserver.api.error.model.ApiError;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDto;
import com.osotnikov.clockserver.subscription.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.osotnikov.clockserver.subscription.api.dto.response.ResourceAffectedResponseDto;

import javax.validation.Valid;

@RestController
@RequestMapping("/subscription")
@Validated
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createSubscription(
            @Valid @RequestBody SubscriptionDto subscriptionDto) {
        log.debug("Received: " + subscriptionDto.toString());

        // This error logic is specific to this endpoint and can be argued that it's not an exceptional condition therefore
        // error logic is based on return value.
        boolean subscriptionCreated = subscriptionService.schedule(subscriptionDto);
        if(!subscriptionCreated) {
            ApiError apiError = new ApiError(HttpStatus.CONFLICT, "Conflicting resource exists.",
                String.format("Resource with name: %s already exists.", subscriptionDto.getPostbackUrl()));
            return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(
            new ResourceAffectedResponseDto(subscriptionDto.getPostbackUrl()), HttpStatus.CREATED);
    }
}
