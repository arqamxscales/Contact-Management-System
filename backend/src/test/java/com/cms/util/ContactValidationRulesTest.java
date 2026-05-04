package com.cms.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContactValidationRules utility class.
 * Tests all validation rules for contact fields (name, email, phone, labels).
 */
@DisplayName("ContactValidationRules Tests")
class ContactValidationRulesTest {

    private static final String WORK = "work";
    private static final String PERSONAL = "personal";
    private static final String HOME = "home";
    private static final String MOBILE = "mobile";

    @Test
    @DisplayName("Should validate correct names")
    void testValidNames() {
        assertNull(ContactValidationRules.validateName("John"));
        assertNull(ContactValidationRules.validateName("Jean-Pierre"));
        assertNull(ContactValidationRules.validateName("O'Brien"));
        assertNull(ContactValidationRules.validateName("Maria Luisa"));
    }

    @Test
    @DisplayName("Should reject invalid names")
    void testInvalidNames() {
        assertNotNull(ContactValidationRules.validateName(null));
        assertNotNull(ContactValidationRules.validateName(""));
        assertNotNull(ContactValidationRules.validateName("123John"));
        assertNotNull(ContactValidationRules.validateName("John@Doe"));
    }

    @Test
    @DisplayName("Should validate correct emails")
    void testValidEmails() {
        assertNull(ContactValidationRules.validateEmail("john@example.com"));
        assertNull(ContactValidationRules.validateEmail("user+tag@domain.co.uk"));
        assertNull(ContactValidationRules.validateEmail("test.email@sub.domain.com"));
    }

    @Test
    @DisplayName("Should reject invalid emails")
    void testInvalidEmails() {
        assertNotNull(ContactValidationRules.validateEmail(null));
        assertNotNull(ContactValidationRules.validateEmail(""));
        assertNotNull(ContactValidationRules.validateEmail("invalid-email"));
        assertNotNull(ContactValidationRules.validateEmail("@domain.com"));
    }

    @Test
    @DisplayName("Should validate correct phone numbers")
    void testValidPhoneNumbers() {
        assertNull(ContactValidationRules.validatePhoneNumber("15551234567"));
        assertNull(ContactValidationRules.validatePhoneNumber("+1-800-555-0123"));
        assertNull(ContactValidationRules.validatePhoneNumber("(555) 123-4567"));
        assertNull(ContactValidationRules.validatePhoneNumber("+441234567890"));
    }

    @Test
    @DisplayName("Should reject invalid phone numbers")
    void testInvalidPhoneNumbers() {
        assertNotNull(ContactValidationRules.validatePhoneNumber(null));
        assertNotNull(ContactValidationRules.validatePhoneNumber(""));
        assertNotNull(ContactValidationRules.validatePhoneNumber("123")); // Too short
        assertNotNull(ContactValidationRules.validatePhoneNumber("ABC-DEF-GHIJ")); // Letters
    }

    @Test
    @DisplayName("Should validate email labels")
    void testValidEmailLabels() {
        assertNull(ContactValidationRules.validateEmailLabel(WORK));
        assertNull(ContactValidationRules.validateEmailLabel(PERSONAL));
        assertNull(ContactValidationRules.validateEmailLabel("other"));
        assertNull(ContactValidationRules.validateEmailLabel("WORK")); // Case-insensitive
    }

    @Test
    @DisplayName("Should reject invalid email labels")
    void testInvalidEmailLabels() {
        assertNotNull(ContactValidationRules.validateEmailLabel(null));
        assertNotNull(ContactValidationRules.validateEmailLabel(""));
        assertNotNull(ContactValidationRules.validateEmailLabel("home"));
        assertNotNull(ContactValidationRules.validateEmailLabel("business"));
    }

    @Test
    @DisplayName("Should validate phone labels")
    void testValidPhoneLabels() {
        assertNull(ContactValidationRules.validatePhoneLabel("work"));
        assertNull(ContactValidationRules.validatePhoneLabel("home"));
        assertNull(ContactValidationRules.validatePhoneLabel(MOBILE));
        assertNull(ContactValidationRules.validatePhoneLabel("other"));
        assertNull(ContactValidationRules.validatePhoneLabel("MOBILE")); // Case-insensitive
    }

    @Test
    @DisplayName("Should reject invalid phone labels")
    void testInvalidPhoneLabels() {
        assertNotNull(ContactValidationRules.validatePhoneLabel(null));
        assertNotNull(ContactValidationRules.validatePhoneLabel(""));
        assertNotNull(ContactValidationRules.validatePhoneLabel("cell"));
        assertNotNull(ContactValidationRules.validatePhoneLabel("office"));
    }

    @Test
    @DisplayName("Should check valid email labels")
    void testIsValidEmailLabel() {
        assertTrue(ContactValidationRules.isValidEmailLabel(WORK));
        assertTrue(ContactValidationRules.isValidEmailLabel(PERSONAL));
        assertTrue(ContactValidationRules.isValidEmailLabel("WORK")); // Case-insensitive
        assertFalse(ContactValidationRules.isValidEmailLabel(HOME));
        assertFalse(ContactValidationRules.isValidEmailLabel(null));
    }

    @Test
    @DisplayName("Should check valid phone labels")
    void testIsValidPhoneLabel() {
        assertTrue(ContactValidationRules.isValidPhoneLabel(WORK));
        assertTrue(ContactValidationRules.isValidPhoneLabel(HOME));
        assertTrue(ContactValidationRules.isValidPhoneLabel(MOBILE));
        assertTrue(ContactValidationRules.isValidPhoneLabel("MOBILE")); // Case-insensitive
        assertFalse(ContactValidationRules.isValidPhoneLabel(PERSONAL));
        assertFalse(ContactValidationRules.isValidPhoneLabel(null));
    }
}
