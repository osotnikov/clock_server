package com.osotnikov.clockserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.CountMatchingMode;
import com.github.tomakehurst.wiremock.client.CountMatchingStrategy;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.osotnikov.clockserver.subscription.api.dto.request.FrequencyDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDeleteDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionDto;
import com.osotnikov.clockserver.subscription.api.dto.request.SubscriptionPatchDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureWireMock(port = 0)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
	"remote.port=${wiremock.server.port}"
})
public class SubscriptionControllerIntegrationTest {

	// port of a stub subscriber to our clock service
	@Value("${remote.port}")
	private String remotePort;

	@Autowired
	private MockMvc mockMvc;

	private String postbackUrl;

	@BeforeEach
	private void setup() {
		postbackUrl = "http://localhost:" + remotePort + "/time-consumer";
		WireMock.reset();
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

		this.mockMvc.perform(
				MockMvcRequestBuilders.post("/subscription")
					.content(asJsonString(new SubscriptionDto(postbackUrl, new FrequencyDto(0, 0, 5))))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value(postbackUrl));

		// await 11s until at least 2 continuous pushes happen
		await().with().timeout(Duration.ofSeconds(20)).pollDelay(Duration.ofSeconds(11)).until(() -> true);
		verify(new CountMatchingStrategy(new CountMatchingMode() {
			@Override
			public boolean test(Integer actual, Integer expected) {
				return actual >= 2 && actual <=3;
			}
			@Override
			public String getFriendlyName() {
				return "custom";
			}
		}, 2), postRequestedFor(urlEqualTo("/time-consumer"))
			.withHeader("Content-Type", equalTo("application/json"))
			.withRequestBody(matchingJsonPath("$.[?(@.currentTime =~ /.+/i)]")));
	}

	@Test
	public void givenSubscriptionExists_whenUserDeletesSubscription_then200() throws Exception {
		this.mockMvc.perform(
				MockMvcRequestBuilders.post("/subscription")
					.content(asJsonString(new SubscriptionDto(postbackUrl, new FrequencyDto(0, 0, 5))))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value(postbackUrl));

		// await 11s until at least 2 continuous pushes happen
		await().pollDelay(Duration.ofSeconds(5)).until(() -> true);
		verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/time-consumer"))
			.withHeader("Content-Type", equalTo("application/json"))
			.withRequestBody(matchingJsonPath("$.[?(@.currentTime =~ /.+/i)]")));

		this.mockMvc.perform(
				MockMvcRequestBuilders.delete("/subscription")
					.content(asJsonString(new SubscriptionDeleteDto(postbackUrl)))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().is(200))
			.andExpect(jsonPath("$.name").value(postbackUrl));

		await().with().timeout(Duration.ofSeconds(30)).pollDelay(Duration.ofSeconds(20)).until(() -> true);

		List<ServeEvent> allServeEvents = getAllServeEvents();
		assertTrue(allServeEvents.size() < 4);
	}

	@Test
	public void givenSubscriptionExists_whenUserEditsSubscription_then200() throws Exception {
		this.mockMvc.perform(
				MockMvcRequestBuilders.post("/subscription")
					.content(asJsonString(new SubscriptionDto(postbackUrl, new FrequencyDto(0, 0, 5))))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value(postbackUrl));

		//await().pollDelay(Duration.ofSeconds(5)).until(() -> true);

		this.mockMvc.perform(
				MockMvcRequestBuilders.patch("/subscription")
					.accept(MediaType.APPLICATION_JSON)
					.content(asJsonString(
						new SubscriptionPatchDto(postbackUrl, new FrequencyDto(1, 1, 1))))
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpect(status().is(200))
			.andExpect(jsonPath("$.name").value(postbackUrl));

		// await 11s until at least 2 continuous pushes happen
		await().with().timeout(Duration.ofSeconds(35)).pollDelay(Duration.ofSeconds(25)).until(() -> true);
		List<ServeEvent> allServeEvents = getAllServeEvents();
		assertTrue(allServeEvents.size() >= 1 && allServeEvents.size() <= 2);
	}

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
