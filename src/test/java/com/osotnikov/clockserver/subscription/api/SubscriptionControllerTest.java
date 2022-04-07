package com.osotnikov.clockserver.subscription.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.osotnikov.clockserver.subscription.api.dto.request.FrequencyDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDto;
import com.osotnikov.clockserver.subscription.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
public class SubscriptionControllerTest {

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
    public void givenPostAndScheduleReturnsTrue_whenUserCreatesNewSubscription_then201() throws Exception {

        SubscriptionDto subscriptionDto = new SubscriptionDto("http://some.postback/url",
            new FrequencyDto(1, 5, 2));
        given(subscriptionService.schedule(subscriptionDto)).willReturn(true);

        this.mockMvc.perform(
            post("/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectWriter.writeValueAsString(subscriptionDto)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"name\":\"http://some.postback/url\"}"));
    }

    @Test
    public void givenPostAndScheduleReturnsFalse_whenUserCreatesNewSubscription_then401() throws Exception {
        SubscriptionDto subscriptionDto = new SubscriptionDto("http://some.postback/url",
            new FrequencyDto(0, 0, 6));
        given(subscriptionService.schedule(subscriptionDto)).willReturn(false);

        this.mockMvc.perform(
                post("/subscription")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectWriter.writeValueAsString(subscriptionDto)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(" {\"status\":\"CONFLICT\",\"message\":\"Conflicting resource exists.\"," +
                "\"errors\":[\"Resource with name: http://some.postback/url already exists.\"]}"));
    }

    @Test
    public void givenPostAndInvalidFrequencyUnderMin_whenUserCreatesNewSubscription_then400() throws Exception {
        SubscriptionDto subscriptionDto = new SubscriptionDto("http://some.postback/url",
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
        SubscriptionDto subscriptionDto = new SubscriptionDto("ptth://some.postback/url",
            new FrequencyDto(0, 0, 6));

        this.mockMvc.perform(
                post("/subscription")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectWriter.writeValueAsString(subscriptionDto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("{\"status\":\"BAD_REQUEST\"," +
                "\"message\":\"Invalid request body.\",\"errors\":[\"must be a valid URL\"]}"));;
    }

    @Test
    public void givenPostAndInvalidPostbackUrlAndInvalidFrequency_whenUserCreatesNewSubscription_then400() throws Exception {
        SubscriptionDto subscriptionDto = new SubscriptionDto("ptth://some.postback/url",
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
                "\"must be a valid URL\"]}"));;
    }

}
