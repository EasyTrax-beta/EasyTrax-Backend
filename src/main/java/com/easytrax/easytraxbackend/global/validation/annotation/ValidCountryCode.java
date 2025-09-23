package com.easytrax.easytraxbackend.global.validation.annotation;

import com.easytrax.easytraxbackend.global.validation.validator.CountryCodeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CountryCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCountryCode {
    String message() default "유효하지 않은 국가 코드입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}