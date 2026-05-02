package com.cms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Phone entity.
 * Tests phone CRUD operations, label management, and primary flag logic.
 */
@DisplayName("Phone Entity Tests")
class PhoneTest {

    private Phone phone;
    private Contact contact;

    @BeforeEach
    void setUp() {
        contact = new Contact();
        contact.setId(1L);
        contact.setFirstName("John");
        contact.setLastName("Doe");

        phone = new Phone();
        phone.setId(1L);
        phone.setNumber("15551234567");
        phone.setLabel("work");
        phone.setPrimary(true);
        phone.setContact(contact);
    }

    @Test
    @DisplayName("Should create phone with all fields")
    void testPhoneCreation() {
        assertNotNull(phone);
        assertEquals("15551234567", phone.getNumber());
        assertEquals("work", phone.getLabel());
        assertTrue(phone.isPrimary());
        assertNotNull(phone.getContact());
    }

    @Test
    @DisplayName("Should set and get phone number")
    void testSetGetNumber() {
        phone.setNumber("+441234567890");
        assertEquals("+441234567890", phone.getNumber());
    }

    @Test
    @DisplayName("Should set and get label")
    void testSetGetLabel() {
        phone.setLabel("home");
        assertEquals("home", phone.getLabel());
        
        phone.setLabel("mobile");
        assertEquals("mobile", phone.getLabel());
    }

    @Test
    @DisplayName("Should set and get primary flag")
    void testSetGetPrimary() {
        phone.setPrimary(false);
        assertFalse(phone.isPrimary());
        
        phone.setPrimary(true);
        assertTrue(phone.isPrimary());
    }

    @Test
    @DisplayName("Should maintain contact relationship")
    void testContactRelationship() {
        Contact newContact = new Contact();
        newContact.setId(2L);
        newContact.setFirstName("Jane");

        phone.setContact(newContact);
        assertEquals(2L, phone.getContact().getId());
    }

    @Test
    @DisplayName("Should validate phone label values")
    void testValidLabels() {
        String[] validLabels = {"work", "home", "mobile", "other"};
        for (String label : validLabels) {
            phone.setLabel(label);
            assertEquals(label, phone.getLabel());
        }
    }

    @Test
    @DisplayName("Phone equality based on ID")
    void testPhoneEquality() {
        Phone phone2 = new Phone();
        phone2.setId(1L);
        phone2.setNumber("19999999999");

        assertEquals(phone, phone2);
    }

    @Test
    @DisplayName("International phone format should be supported")
    void testInternationalFormat() {
        String[] internationalNumbers = {
            "+441234567890",
            "+33123456789",
            "+81312345678"
        };
        
        for (String number : internationalNumbers) {
            phone.setNumber(number);
            assertEquals(number, phone.getNumber());
        }
    }

    @Test
    @DisplayName("Phone without contact should be allowed")
    void testPhoneWithoutContact() {
        Phone orphanPhone = new Phone();
        orphanPhone.setId(2L);
        orphanPhone.setNumber("15559999999");
        orphanPhone.setLabel("work");
        orphanPhone.setContact(null);

        assertNull(orphanPhone.getContact());
        assertNotNull(orphanPhone.getNumber());
    }
}
