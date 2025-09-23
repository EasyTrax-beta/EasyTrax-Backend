package com.easytrax.easytraxbackend.global.validation.validator;

import com.easytrax.easytraxbackend.global.validation.annotation.ValidCountryCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class CountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {
    
    private static final Set<String> VALID_COUNTRY_CODES = Set.of(
            "CN", "US", "JP", "EU"
    );

    @Override
    public void initialize(ValidCountryCode constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return VALID_COUNTRY_CODES.contains(value.toUpperCase());
    }
}