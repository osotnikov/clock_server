package com.osotnikov.subscription.controller;

import com.osotnikov.subscription.controller.dto.SubscriptionDto;
import com.osotnikov.subscription.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.osotnikov.response.ResourceAffectedResponseDto;

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
    public ResponseEntity<ResourceAffectedResponseDto> createSubscription(
            @RequestBody SubscriptionDto subscriptionDto) {

        log.debug("Received: " + subscriptionDto.toString());
        return new ResponseEntity<>(
            new ResourceAffectedResponseDto(subscriptionDto.getPostbackUrl()), HttpStatus.CREATED);
    }
}
