package com.cms.util;

/**
 * Centralized validation rules for Contact entity.
 * Defines constraints for phone numbers, emails, and names to ensure data quality.
 * Used across service layer and DTOs for consistent validation logic.
 */
public class ContactValidationRules {

    private static final String CHARACTERS_SUFFIX = " characters";

    // Name validation - allow alphanumeric, spaces, hyphens, apostrophes
    // Example: "John", "Jean-Pierre", "O'Brien", "Maria Luisa"
    public static final String NAME_PATTERN = "^[a-zA-Z\\s'-]{1,100}$";
    public static final String NAME_ERROR_MSG = "Name must be 1-100 characters, letters/spaces/hyphens/apostrophes only";
    public static final int NAME_MIN_LENGTH = 1;
    public static final int NAME_MAX_LENGTH = 100;

    // Email validation - standard RFC 5322 simplified pattern
    // Already provided by @Email annotation, but pattern shown here for reference
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String EMAIL_ERROR_MSG = "Email must be a valid email address";
    public static final int EMAIL_MAX_LENGTH = 150;

    // Phone validation - allows digits, +, -, (, ), spaces
    // Flexible to support international formats: +1-800-555-0123, (555) 0123, etc.
    public static final String PHONE_PATTERN = "^[\\d+\\-()\\s]{10,}$";
    public static final String PHONE_ERROR_MSG = "Phone must be at least 10 digits (including formatting)";
    public static final int PHONE_MIN_LENGTH = 10;
    public static final int PHONE_MAX_LENGTH = 25;

    // Address validation - allow alphanumeric and common address characters
    public static final String ADDRESS_PATTERN = "^[a-zA-Z0-9\\s,'.#-]*$";
    public static final String ADDRESS_ERROR_MSG = "Address contains invalid characters";
    public static final int ADDRESS_MAX_LENGTH = 255;

    // Company/Title validation - alphanumeric with common business characters
    public static final String TITLE_PATTERN = "^[a-zA-Z\\s&,.-]*$";
    public static final String TITLE_ERROR_MSG = "Title must contain only letters, spaces, and common business characters";
    public static final int TITLE_MAX_LENGTH = 100;

    // Email label validation - must be one of these predefined values
    protected static final String[] VALID_EMAIL_LABELS = {"work", "personal", "other"};
    public static final String EMAIL_LABEL_ERROR_MSG = "Email label must be 'work', 'personal', or 'other'";

    // Phone label validation - must be one of these predefined values
    protected static final String[] VALID_PHONE_LABELS = {"work", "home", "mobile", "other"};
    public static final String PHONE_LABEL_ERROR_MSG = "Phone label must be 'work', 'home', 'mobile', or 'other'";

    private ContactValidationRules() {
    }

    /**
     * Validate name format according to contact name rules.
     * Returns error message if invalid, null if valid.
     */
    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Name is required";
        }
        if (name.length() < NAME_MIN_LENGTH || name.length() > NAME_MAX_LENGTH) {
            return "Name must be between " + NAME_MIN_LENGTH + " and " + NAME_MAX_LENGTH + CHARACTERS_SUFFIX;
        }
        if (!name.matches(NAME_PATTERN)) {
            return NAME_ERROR_MSG;
        }
        return null; // Valid
    }

    /**
     * Validate email format according to contact email rules.
     * Returns error message if invalid, null if valid.
     */
    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        if (email.length() > EMAIL_MAX_LENGTH) {
            return "Email must be less than " + EMAIL_MAX_LENGTH + CHARACTERS_SUFFIX;
        }
        if (!email.matches(EMAIL_PATTERN)) {
            return EMAIL_ERROR_MSG;
        }
        return null; // Valid
    }

    /**
     * Validate phone number format according to contact phone rules.
     * Returns error message if invalid, null if valid.
     */
    public static String validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Phone number is required";
        }
        
        // Count only digits to check minimum requirement
        String digitsOnly = phone.replaceAll("\\D", "");
        if (digitsOnly.length() < PHONE_MIN_LENGTH) {
            return "Phone must contain at least " + PHONE_MIN_LENGTH + " digits";
        }
        
        if (phone.length() > PHONE_MAX_LENGTH) {
            return "Phone must be less than " + PHONE_MAX_LENGTH + CHARACTERS_SUFFIX;
        }
        
        if (!phone.matches(PHONE_PATTERN)) {
            return PHONE_ERROR_MSG;
        }
        return null; // Valid
    }

    /**
     * Validate address format according to contact address rules.
     * Returns error message if invalid, null if valid.
     */
    public static String validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null; // Address is optional
        }
        if (address.length() > ADDRESS_MAX_LENGTH) {
            return "Address must be less than " + ADDRESS_MAX_LENGTH + CHARACTERS_SUFFIX;
        }
        if (!address.matches(ADDRESS_PATTERN)) {
            return ADDRESS_ERROR_MSG;
        }
        return null; // Valid
    }

    /**
     * Validate email label is one of the allowed values.
     * Returns error message if invalid, null if valid.
     */
    public static String validateEmailLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            return "Email label is required";
        }
        for (String validLabel : VALID_EMAIL_LABELS) {
            if (validLabel.equalsIgnoreCase(label)) {
                return null; // Valid
            }
        }
        return EMAIL_LABEL_ERROR_MSG;
    }

    /**
     * Validate phone label is one of the allowed values.
     * Returns error message if invalid, null if valid.
     */
    public static String validatePhoneLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            return "Phone label is required";
        }
        for (String validLabel : VALID_PHONE_LABELS) {
            if (validLabel.equalsIgnoreCase(label)) {
                return null; // Valid
            }
        }
        return PHONE_LABEL_ERROR_MSG;
    }

    /**
     * Check if email label is valid.
     */
    public static boolean isValidEmailLabel(String label) {
        if (label == null) return false;
        for (String valid : VALID_EMAIL_LABELS) {
            if (valid.equalsIgnoreCase(label)) return true;
        }
        return false;
    }

    /**
     * Check if phone label is valid.
     */
    public static boolean isValidPhoneLabel(String label) {
        if (label == null) return false;
        for (String valid : VALID_PHONE_LABELS) {
            if (valid.equalsIgnoreCase(label)) return true;
        }
        return false;
    }
}
