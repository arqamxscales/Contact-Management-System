package com.cms.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LabeledEmailDto and LabeledPhoneDto validation.
 */
@DisplayName("Labeled Email and Phone DTO Tests")
class LabeledContactDtoTest {

    private final Validator validator;

    public LabeledContactDtoTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    @DisplayName("LabeledEmailDto should validate with valid data")
    void testValidEmail() {
        LabeledEmailDto dto = new LabeledEmailDto("work", "john@example.com", true);
        Set<ConstraintViolation<LabeledEmailDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("LabeledEmailDto should fail with invalid email format")
    void testInvalidEmailFormat() {
        LabeledEmailDto dto = new LabeledEmailDto("work", "invalid-email", true);
        Set<ConstraintViolation<LabeledEmailDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("LabeledEmailDto should require label")
    void testEmailMissingLabel() {
        LabeledEmailDto dto = new LabeledEmailDto("", "john@example.com", true);
        Set<ConstraintViolation<LabeledEmailDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("LabeledPhoneDto should validate with valid data")
    void testValidPhone() {
        LabeledPhoneDto dto = new LabeledPhoneDto("work", "15551234567", true);
        Set<ConstraintViolation<LabeledPhoneDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("LabeledPhoneDto should accept international format")
    void testValidPhoneInternational() {
        LabeledPhoneDto dto = new LabeledPhoneDto("work", "+441234567890", true);
        Set<ConstraintViolation<LabeledPhoneDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("LabeledPhoneDto should fail with invalid phone format")
    void testInvalidPhoneFormat() {
        LabeledPhoneDto dto = new LabeledPhoneDto("work", "123", true);
        Set<ConstraintViolation<LabeledPhoneDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("LabeledPhoneDto should require label")
    void testPhoneMissingLabel() {
        LabeledPhoneDto dto = new LabeledPhoneDto("", "15551234567", true);
        Set<ConstraintViolation<LabeledPhoneDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
