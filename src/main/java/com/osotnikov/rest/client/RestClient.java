package com.osotnikov.rest.client;

import org.apache.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Abstracts the logic to make remote rest requests
 * */
public class RestClient<T> {

	private WebClient webClient = WebClient.builder()
		.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.build();

	/**
	 * returns true if successful, false otherwise, ignores response body
	 * It's simplified for the purposes of this sample,
	 * Ideally it should map to a ResponseDto that's independent of underlying implementation.
	 * */
	public boolean postAndIgnoreResponseBody(String uri, T bodyContent) {
		try {
			ResponseEntity<Void> r = webClient
				.post()
				.uri(uri)
				.bodyValue(bodyContent)
				.retrieve()
				.toBodilessEntity()
				.block();
			return !r.getStatusCode().isError();
		} catch (Exception e) {
			// on 500 internal ClientHttpConnector throws an exception hence we need the catch, known bug cannot handle
			// in async stream with onErrorContinue ...
			return false;
		}
	}
}
