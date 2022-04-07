package com.osotnikov.clockserver.subscription.api.validation;

import com.osotnikov.clockserver.subscription.api.dto.request.FrequencyDto;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class PostbackFrequencyValidatorTest {

	private PostbackFrequencyValidator postbackFrequencyValidator = new PostbackFrequencyValidator();

	@ParameterizedTest()
	@MethodSource("com.osotnikov.clockserver.subscription.api.validation.PostbackFrequencyValidatorTest#invalidFrequencyDto")
	public void givenInvalidFrequencyDtos_whenIsValidCalled_thenReturnsFalse(FrequencyDto frequencyDto) {
		assertFalse(postbackFrequencyValidator.isValid(frequencyDto, null));
	}

	private static Stream<Arguments> invalidFrequencyDto() {
		return Stream.of(
			Arguments.of(new FrequencyDto(-1, -1, -1)),
			Arguments.of(new FrequencyDto(0, 0, 0)),
			Arguments.of(new FrequencyDto(0, 0, 2),
			Arguments.of(new FrequencyDto(1, -1, 6)),
			Arguments.of(new FrequencyDto(1, -1, 6)),
			Arguments.of(new FrequencyDto(0, 0, 4)),
			Arguments.of(new FrequencyDto(4, 0, 1)),
			Arguments.of(new FrequencyDto(3, 60, 59)),
			Arguments.of(new FrequencyDto(3, 58, 60)),
			Arguments.of(new FrequencyDto(3, 61, 6)),
			Arguments.of(new FrequencyDto(3, 56, 61))
		));
	}

	@ParameterizedTest(name = "{1}")
	@MethodSource("com.osotnikov.clockserver.subscription.api.validation.PostbackFrequencyValidatorTest#validFrequencyDto")
	public void givenValidFrequencyDtos_whenIsValidCalled_thenReturnsTrue(FrequencyDto frequencyDto) {
		assertTrue(postbackFrequencyValidator.isValid(frequencyDto, null));
	}

	private static Stream<Arguments> validFrequencyDto() {
		return Stream.of(
			Arguments.of(new FrequencyDto(4, 0, 0)),
			Arguments.of(new FrequencyDto(0, 0, 5)),
			Arguments.of(new FrequencyDto(1, 0, 2),
			Arguments.of(new FrequencyDto(3, 1, 4)),
			Arguments.of(new FrequencyDto(3, 59, 59))
		));
	}

}
