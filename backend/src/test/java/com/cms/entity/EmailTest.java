package com.cms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Email entity.
 * Tests email CRUD operations, label management, and primary flag logic.
 */
@DisplayName("Email Entity Tests")
class EmailTest {

    private Email email;
    private Contact contact;

    @BeforeEach
    void setUp() {
        contact = new Contact();
        contact.setId(1L);
        contact.setFirstName("John");
        contact.setLastName("Doe");

        email = new Email();
        email.setId(1L);
        email.setAddress("john@example.com");
        email.setLabel("work");
        email.setPrimary(true);
        email.setContact(contact);
    }

    @Test
    @DisplayName("Should create email with all fields")
    void testEmailCreation() {
        assertNotNull(email);
        assertEquals("john@example.com", email.getAddress());
        assertEquals("work", email.getLabel());
        assertTrue(email.isPrimary());
        assertNotNull(email.getContact());
    }

    @Test
    @DisplayName("Should set and get address")
    void testSetGetAddress() {
        email.setAddress("newemail@example.com");
        assertEquals("newemail@example.com", email.getAddress());
    }

    @Test
    @DisplayName("Should set and get label")
    void testSetGetLabel() {
        email.setLabel("personal");
        assertEquals("personal", email.getLabel());
    }

    @Test
    @DisplayName("Should set and get primary flag")
    void testSetGetPrimary() {
        email.setPrimary(false);
        assertFalse(email.isPrimary());
        
        email.setPrimary(true);
        assertTrue(email.isPrimary());
    }

    @Test
    @DisplayName("Should maintain contact relationship")
    void testContactRelationship() {
        Contact newContact = new Contact();
        newContact.setId(2L);
        newContact.setFirstName("Jane");

        email.setContact(newContact);
        assertEquals(2L, email.getContact().getId());
    }

    @Test
    @DisplayName("Should validate email label values")
    void testValidLabels() {
        String[] validLabels = {"work", "personal", "other"};
        for (String label : validLabels) {
            email.setLabel(label);
            assertEquals(label, email.getLabel());
        }
    }

    @Test
    @DisplayName("Email equality based on ID")
    void testEmailEquality() {
        Email email2 = new Email();
        email2.setId(1L);
        email2.setAddress("different@example.com");

        assertEquals(email, email2);
    }

    @Test
    @DisplayName("Email with null contact should be allowed")
    void testEmailWithoutContact() {
        Email orphanEmail = new Email();
        orphanEmail.setId(2L);
        orphanEmail.setAddress("orphan@example.com");
        orphanEmail.setLabel("work");
        orphanEmail.setContact(null);

        assertNull(orphanEmail.getContact());
        assertNotNull(orphanEmail.getAddress());
    }
}
