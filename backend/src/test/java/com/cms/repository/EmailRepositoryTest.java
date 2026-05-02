package com.cms.repository;

import com.cms.entity.Contact;
import com.cms.entity.Email;
import com.cms.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailRepository.
 * Tests custom query methods for email lookups and filtering.
 */
@DataJpaTest
@DisplayName("EmailRepository Tests")
class EmailRepositoryTest {

    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    private Contact contact;
    private Email email1;
    private Email email2;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setPassword("hashedpassword");
        user = userRepository.save(user);

        contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setUser(user);
        contact = contactRepository.save(contact);

        email1 = new Email();
        email1.setAddress("john.work@example.com");
        email1.setLabel("work");
        email1.setPrimary(true);
        email1.setContact(contact);
        email1 = emailRepository.save(email1);

        email2 = new Email();
        email2.setAddress("john.personal@example.com");
        email2.setLabel("personal");
        email2.setPrimary(false);
        email2.setContact(contact);
        email2 = emailRepository.save(email2);
    }

    @Test
    @DisplayName("Should find emails by contact")
    void testFindByContact() {
        List<Email> emails = emailRepository.findByContact(contact);
        assertEquals(2, emails.size());
        assertTrue(emails.stream().anyMatch(e -> e.getAddress().equals("john.work@example.com")));
    }

    @Test
    @DisplayName("Should find emails by label")
    void testFindByLabel() {
        List<Email> workEmails = emailRepository.findByLabel("work");
        assertTrue(workEmails.stream().anyMatch(e -> e.getAddress().equals("john.work@example.com")));
    }

    @Test
    @DisplayName("Should find primary email by contact")
    void testFindPrimaryByContact() {
        List<Email> primaryEmails = emailRepository.findByContactAndPrimaryTrue(contact);
        assertEquals(1, primaryEmails.size());
        assertTrue(primaryEmails.get(0).isPrimary());
    }

    @Test
    @DisplayName("Should find duplicate email addresses")
    void testFindDuplicates() {
        Email duplicate = new Email();
        duplicate.setAddress("john.work@example.com");
        duplicate.setLabel("other");
        duplicate.setContact(contact);
        emailRepository.save(duplicate);

        List<Email> duplicates = emailRepository.findByAddress("john.work@example.com");
        assertEquals(2, duplicates.size());
    }

    @Test
    @DisplayName("Should return empty list for non-existent contact")
    void testFindByContactNotFound() {
        Contact nonExistent = new Contact();
        nonExistent.setId(9999L);
        List<Email> emails = emailRepository.findByContact(nonExistent);
        assertTrue(emails.isEmpty());
    }
}
