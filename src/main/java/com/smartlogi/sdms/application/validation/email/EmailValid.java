package com.smartlogi.sdms.application.validation.email;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.*;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy=EmailValidator.class)
public @interface EmailValid {
    String message() default "Invalid Email Address";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
