package com.ugurhicyilmam.controller.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidImpl.class)
public @interface EmailValid {

    String message() default "{com.ugurhicyilmam.constraint.EmailValid.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
