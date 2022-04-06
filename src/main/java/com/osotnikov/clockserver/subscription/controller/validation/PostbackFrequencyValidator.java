package com.osotnikov.clockserver.subscription.controller.validation;

import com.osotnikov.clockserver.subscription.controller.dto.FrequencyDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PostbackFrequencyValidator implements ConstraintValidator<PostbackFrequencyConstraint, FrequencyDto> {

	@Override
	public void initialize(PostbackFrequencyConstraint contactNumber) {
	}

	@Override
	public boolean isValid(FrequencyDto frequencyString,
						   ConstraintValidatorContext cxt) {

		if(frequencyString.getHours() > 4 ||
		   (frequencyString.getHours() == 4 && (frequencyString.getMinutes() > 0 || frequencyString.getSeconds() > 0)) ||
		   frequencyString.getHours() <= 0 && frequencyString.getMinutes() <= 0 && frequencyString.getSeconds() <= 6) {
			return false;
		}
		return true;
	}

}
