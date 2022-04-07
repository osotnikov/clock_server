package com.osotnikov.rest.client;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

/**
 * Abstracts the logic to make remote rest requests
 * */
@Component
@Slf4j
public class RestClient<T> {

	private WebClient webClient = WebClient.builder()
		.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.build();
	private Class<T> clazz;

	// Need to supply the type of the post body content because of type erasure.
	public RestClient(Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * returns true if successful, false otherwise, ignores response body
	 * It's simplified for the purposes of this sample,
	 * Ideally it should map to a ResponseDto that's independent of underlying implementation.
	 * */
	public boolean postAndIgnoreResponseBody(String uri, T bodyContent) {
		ResponseEntity<Void> r = webClient
			.post()
			.uri(uri)
			.bodyValue(bodyContent)
			.retrieve()
			.toBodilessEntity()
			.block();
		return !r.getStatusCode().isError();
	}
}
