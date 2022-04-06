package com.osotnikov.clockserver.subscription.controller.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.*;

@Documented
@Constraint(validatedBy = PostbackFrequencyValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RUNTIME)
public @interface PostbackFrequencyConstraint {
	String message() default "Invalid frequency object. Must be between 5 seconds and 4 hours.";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
