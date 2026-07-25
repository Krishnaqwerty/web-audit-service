package com.webaudit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = UrlValidatorConstraint.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {
    String message() default "Must be a valid HTTP or HTTPS URL and must not target internal or restricted IP addresses";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
