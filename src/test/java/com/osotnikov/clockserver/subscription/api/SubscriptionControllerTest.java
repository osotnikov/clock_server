package com.osotnikov.clockserver.subscription.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.osotnikov.clockserver.subscription.api.dto.request.FrequencyDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDeleteDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDto;
import com.osotnikov.clockserver.subscription.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
public class SubscriptionControllerTest {
    
    private static final String VALID_POSTBACK_URL = "http://some.postback/url";
    private static final String INVALID_POSTBACK_URL = "ptth://some.postback/url";

    private static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    }
    private static ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();

    @MockBean
    private SubscriptionService subscriptionService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void givenPostAndExistingSubscription_whenUserCreatesNewSubscription_then201() throws Exception {

        SubscriptionDto subscriptionDto = new SubscriptionDto(VALID_POSTBACK_URL,
            new FrequencyDto(1, 5, 2));
        given(subscriptionService.schedule(subscriptionDto)).willReturn(true);

        this.mockMvc.perform(
            post("/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectWriter.writeValueAsString(subscriptionDto)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(String.format("{\"name\":\"%s\"}", VALID_POSTBACK_URL)));
    }

    @Test
    public void givenPostAndNonExistingSubscription_whenUserCreatesNewSubscription_then401() throws Exception {
        SubscriptionDto subscriptionDto = new SubscriptionDto(VALID_POSTBACK_URL,
            new FrequencyDto(0, 0, 6));
        given(subscriptionService.schedule(subscriptionDto)).willReturn(false);

        this.mockMvc.perform(
                post("/subscription")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectWriter.writeValueAsString(subscriptionDto)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(String.format("{\"status\":\"CONFLICT\",\"message\":\"Conflicting resource exists.\"," +
                "\"errors\":[\"Resource with name: %s already exists.\"]}", VALID_POSTBACK_URL)));
    }

    @Test
    public void givenPostAndInvalidFrequencyUnderMin_whenUserCreatesNewSubscription_then400() throws Exception {
        SubscriptionDto subscriptionDto = new SubscriptionDto(VALID_POSTBACK_URL,
            new FrequencyDto(0, 0, 2));

        this.mockMvc.perform(
            post("/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectWriter.writeValueAsString(subscriptionDto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"status\":\"BAD_REQUEST\",\"message\":\"Invalid request body.\"," +
                "\"errors\":[\"Invalid frequency object. Must be between 5 seconds and 4 hours.\"]}"));
    }

    @Test
    public void givenPostAndInvalidPostbackUrl_whenUserCreatesNewSubscription_then400() throws Exception {
        SubscriptionDto subscriptionDto = new SubscriptionDto(INVALID_POSTBACK_URL,
            new FrequencyDto(0, 0, 6));

        this.mockMvc.perform(
                post("/subscription")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectWriter.writeValueAsString(subscriptionDto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"status\":\"BAD_REQUEST\"," +
                "\"message\":\"Invalid request body.\",\"errors\":[\"must be a valid URL\"]}"));
    }

    @Test
    public void givenPostAndInvalidPostbackUrlAndInvalidFrequency_whenUserCreatesNewSubscription_then400() throws Exception {
        SubscriptionDto subscriptionDto = new SubscriptionDto(INVALID_POSTBACK_URL,
            new FrequencyDto(0, 0, 4));

        this.mockMvc.perform(
                post("/subscription")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectWriter.writeValueAsString(subscriptionDto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(
                "{\"status\":\"BAD_REQUEST\"," +
                "\"message\":\"Invalid request body.\"," +
                "\"errors\":[\"Invalid frequency object. Must be between 5 seconds and 4 hours.\"," +
                "\"must be a valid URL\"]}"));
    }

    @Test
    public void givenDeleteExistingSubscriptionAndValidUrl_whenDelete_then200() throws Exception {

        SubscriptionDeleteDto subscriptionDeleteDto = new SubscriptionDeleteDto(VALID_POSTBACK_URL);
        given(subscriptionService.delete(VALID_POSTBACK_URL)).willReturn(true);

        this.mockMvc.perform(
                delete("/subscription")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectWriter.writeValueAsString(subscriptionDeleteDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(String.format("{\"name\":\"%s\"}", VALID_POSTBACK_URL)));
    }

    @Test
    public void givenDeleteNonExistingSubscriptionAndValidUrl_whenDelete_then404() throws Exception {

        SubscriptionDeleteDto subscriptionDeleteDto = new SubscriptionDeleteDto(VALID_POSTBACK_URL);
        given(subscriptionService.delete(VALID_POSTBACK_URL)).willReturn(false);

        this.mockMvc.perform(
                delete("/subscription")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectWriter.writeValueAsString(subscriptionDeleteDto)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(
                String.format("{\"status\":\"NOT_FOUND\",\"message\":\"No resource exists.\"," +
                    "\"errors\":[\"Resource with name: %s does not exist.\"]}", VALID_POSTBACK_URL)));
    }

    @Test
    public void givenDeleteExistingSubscriptionAndInvalidUrl_whenDelete_then400() throws Exception {
        SubscriptionDeleteDto subscriptionDto = new SubscriptionDeleteDto(INVALID_POSTBACK_URL);
        given(subscriptionService.delete(VALID_POSTBACK_URL)).willReturn(true);

        this.mockMvc.perform(
                delete("/subscription")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectWriter.writeValueAsString(subscriptionDto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"status\":\"BAD_REQUEST\"," +
                "\"message\":\"Invalid request body.\",\"errors\":[\"must be a valid URL\"]}"));
    }

}
