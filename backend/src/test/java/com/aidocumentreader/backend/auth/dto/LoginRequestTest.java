package com.aidocumentreader.backend.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest{
    private static Validator validator;

    @BeforeAll
    static void setUpValidator(){
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()){
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldPassValidationWhenFieldsAreValid(){
        LoginRequest request = new LoginRequest("user@example.com", "StrongPassword123!");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenEmailIsInvalid(){
        LoginRequest request = new LoginRequest("not-an-email", "StrongPassword123!");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailValidationWhenPasswordIsBlank(){
        LoginRequest request = new LoginRequest("user@example.com", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }
}