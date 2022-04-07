package com.osotnikov.rest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osotnikov.clockserver.subscription.service.model.rest.client.dto.request.CurrentTimeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD) // just in case parallel execution is configured as default in the future, make this test an exception
public class RestClientTest {

	private static final String MOCK_BASE_URL = "http://localhost:%s";

	private static MockWebServer mockWebServer;
	private static RestClient<CurrentTimeDto> restClient;

	@BeforeAll
	static void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		restClient = new RestClient(CurrentTimeDto.class);
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockWebServer.shutdown();
		restClient = null;
	}

	@Test
	void givenPostCalledWithCurrentTimeDto_whenPostCalled_thenSuccessAndValidJsonWasPostedAndResponseBodyIgnored() throws Exception {

		String expectedPostbackUrl = String.format(MOCK_BASE_URL, mockWebServer.getPort());

		ObjectMapper objectMapper = new ObjectMapper();
		mockWebServer.enqueue(new MockResponse()
			.setBody(objectMapper.writeValueAsString(new DummyResponse("some response message")))
			.addHeader("Content-Type", "application/json"));

		String expectedTimeStr = "2022-06-05 16:30:06";
		boolean success = restClient.postAndIgnoreResponseBody(expectedPostbackUrl, new CurrentTimeDto(expectedTimeStr));

		assertTrue(success);
		RecordedRequest request = mockWebServer.takeRequest();
		assertThat(request.getMethod()).isEqualTo("POST");
		assertEquals("localhost", request.getRequestUrl().host().replace("127.0.0.1", "localhost"));
		assertEquals(mockWebServer.getPort(), request.getRequestUrl().port());
		assertEquals("http", request.getRequestUrl().scheme());
		String postedTimeStr = request.getBody().readUtf8();
		CurrentTimeDto postedTimeCurrentDto = objectMapper.readValue(postedTimeStr, CurrentTimeDto.class);
		assertEquals(expectedTimeStr, postedTimeCurrentDto.getCurrentTime());
	}

	@Data
	@AllArgsConstructor
	private static class DummyResponse {
		private String message;
	}
}
