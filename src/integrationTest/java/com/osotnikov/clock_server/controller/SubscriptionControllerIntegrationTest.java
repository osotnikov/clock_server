package com.osotnikov.clock_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osotnikov.clockserver.subscription.controller.dto.FrequencyDto;
import com.osotnikov.clockserver.subscription.controller.dto.SubscriptionDto;
import com.osotnikov.clockserver.subscription.controller.dto.SubscriptionPatchDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
	"remote1.port=${wiremock.server.port}"
})
public class SubscriptionControllerIntegrationTest {

	// port of a stub subscriber to our clock service
	@Value("${remote1.port}")
	private String remotePort;

	@Autowired
	private MockMvc mockMvc;

	private String postbackUrl;

	@BeforeEach
	private void setup() {

	}

	@Test
	public void givenTimeConsumerEndpointConsumesSuccessfully_whenUserCreatesNewSubscription_then201() throws Exception {

		// stub out a subscriber to our clock service
		stubFor(post(urlEqualTo("/time-consumer"))
			.willReturn(
				aResponse()
					.withStatus(200)
					.withHeader("Content-Type", "application/json")
					.withBody("{\"status\": \"OK\"}"))
		);

		String postbackUrl = "http://localhost:" + remotePort + "/time-consumer";

		this.mockMvc.perform(
				MockMvcRequestBuilders.post("/subscription")
					.content(asJsonString(new SubscriptionDto(postbackUrl, new FrequencyDto(0, 0, 5))))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value(postbackUrl));

		// TODO: await until continuous pushes happen
		// TODO: verify(postRequestedFor(urlEqualTo("/time-consumer")));
	}

	@Test
	public void givenSubscriptionExists_whenUserDeletesSubscription_then200() throws Exception {
		this.mockMvc.perform(
				MockMvcRequestBuilders.delete("/subscription")
					.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().is(200))
			.andExpect(jsonPath("$.name").value(postbackUrl));
	}

	@Test
	public void givenSubscriptionExists_whenUserEditsSubscription_then200() throws Exception {
		this.mockMvc.perform(
				MockMvcRequestBuilders.patch("/subscription")
					.accept(MediaType.APPLICATION_JSON)
					.content(asJsonString(new SubscriptionPatchDto(new FrequencyDto(0, 0, 5))))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().is(200))
			.andExpect(jsonPath("$.name").value(postbackUrl));
	}

	@Test
	public void givenSubscriptionExists_whenUserReplacesSubscription_then200() throws Exception {
		this.mockMvc.perform(
				MockMvcRequestBuilders.put("/subscription")
					.accept(MediaType.APPLICATION_JSON)
					.content(asJsonString(new SubscriptionDto(postbackUrl, new FrequencyDto(0, 0, 5))))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().is(200))
			.andExpect(jsonPath("$.name").value(postbackUrl));
	}

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
