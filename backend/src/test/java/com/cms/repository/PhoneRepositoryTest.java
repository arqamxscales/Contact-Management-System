package com.cms.repository;

import com.cms.entity.Contact;
import com.cms.entity.Phone;
import com.cms.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PhoneRepository.
 * Tests custom query methods for phone lookups and filtering.
 */
@DataJpaTest
@DisplayName("PhoneRepository Tests")
class PhoneRepositoryTest {

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    private Contact contact;
    private Phone phone1;
    private Phone phone2;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setPassword("hashedpassword");
        user = userRepository.save(user);

        contact = new Contact();
        contact.setFirstName("Jane");
        contact.setLastName("Smith");
        contact.setUser(user);
        contact = contactRepository.save(contact);

        phone1 = new Phone();
        phone1.setNumber("15551234567");
        phone1.setLabel("work");
        phone1.setPrimary(true);
        phone1.setContact(contact);
        phone1 = phoneRepository.save(phone1);

        phone2 = new Phone();
        phone2.setNumber("15559876543");
        phone2.setLabel("mobile");
        phone2.setPrimary(false);
        phone2.setContact(contact);
        phone2 = phoneRepository.save(phone2);
    }

    @Test
    @DisplayName("Should find phones by contact")
    void testFindByContact() {
        List<Phone> phones = phoneRepository.findByContact(contact);
        assertEquals(2, phones.size());
        assertTrue(phones.stream().anyMatch(p -> p.getNumber().equals("15551234567")));
    }

    @Test
    @DisplayName("Should find phones by label")
    void testFindByLabel() {
        List<Phone> mobilePhones = phoneRepository.findByLabel("mobile");
        assertTrue(mobilePhones.stream().anyMatch(p -> p.getNumber().equals("15559876543")));
    }

    @Test
    @DisplayName("Should find primary phone by contact")
    void testFindPrimaryByContact() {
        List<Phone> primaryPhones = phoneRepository.findByContactAndPrimaryTrue(contact);
        assertEquals(1, primaryPhones.size());
        assertTrue(primaryPhones.get(0).isPrimary());
    }

    @Test
    @DisplayName("Should find duplicate phone numbers")
    void testFindDuplicates() {
        Phone duplicate = new Phone();
        duplicate.setNumber("15551234567");
        duplicate.setLabel("other");
        duplicate.setContact(contact);
        phoneRepository.save(duplicate);

        List<Phone> duplicates = phoneRepository.findByNumber("15551234567");
        assertEquals(2, duplicates.size());
    }

    @Test
    @DisplayName("Should support international format")
    void testInternationalPhone() {
        Phone intlPhone = new Phone();
        intlPhone.setNumber("+441234567890");
        intlPhone.setLabel("work");
        intlPhone.setContact(contact);
        intlPhone = phoneRepository.save(intlPhone);

        Phone found = phoneRepository.findByNumber("+441234567890").get(0);
        assertEquals("+441234567890", found.getNumber());
    }

    @Test
    @DisplayName("Should return empty list for non-existent contact")
    void testFindByContactNotFound() {
        Contact nonExistent = new Contact();
        nonExistent.setId(9999L);
        List<Phone> phones = phoneRepository.findByContact(nonExistent);
        assertTrue(phones.isEmpty());
    }
}
