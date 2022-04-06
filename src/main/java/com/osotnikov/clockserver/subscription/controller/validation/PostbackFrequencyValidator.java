package com.osotnikov.clockserver.subscription.controller.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PostbackFrequencyValidator implements ConstraintValidator<PostbackFrequencyConstraint, String> {

	@Override
	public void initialize(PostbackFrequencyConstraint contactNumber) {
	}

	@Override
	public boolean isValid(String frequencyString,
						   ConstraintValidatorContext cxt) {



		return frequencyString != null && frequencyString.matches("[hm][\\d]+[d]")
			&& (frequencyString.length() > 8) && (frequencyString.length() < 14);
	}

}
