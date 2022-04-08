package com.osotnikov.clockserver.subscription.api;

import com.osotnikov.clockserver.api.error.model.ApiError;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDeleteDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionPatchDto;
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
    public ResponseEntity<?> createSubscription(@Valid @RequestBody SubscriptionDto subscriptionDto) {
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

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteSubscription(@Valid @RequestBody SubscriptionDeleteDto subscriptionDeleteDto) {
        log.debug("Received: " + subscriptionDeleteDto.toString());

        boolean subscriptionDeleted = subscriptionService.delete(subscriptionDeleteDto.getName());
        if(!subscriptionDeleted) {
            ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "No resource exists.",
                String.format("Resource with name: %s does not exist.", subscriptionDeleteDto.getName()));
            return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
            new ResourceAffectedResponseDto(subscriptionDeleteDto.getName()), HttpStatus.OK);
    }

    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeSubscription(@Valid @RequestBody SubscriptionPatchDto subscriptionPatchDto) {
        log.debug("Received: " + subscriptionPatchDto.toString());

        boolean subscriptionChanged = subscriptionService.changeSchedule(subscriptionPatchDto);
        if(!subscriptionChanged) {
            ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "No resource exists.",
                String.format("Resource with name: %s does not exist.", subscriptionPatchDto.getName()));
            return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
            new ResourceAffectedResponseDto(subscriptionPatchDto.getName()), HttpStatus.OK);
    }
}
