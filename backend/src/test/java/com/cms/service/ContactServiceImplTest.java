package com.cms.service;

import com.cms.dto.ContactRequest;
import com.cms.dto.ContactResponse;
import com.cms.entity.Contact;
import com.cms.entity.User;
import com.cms.exception.ResourceNotFoundException;
import com.cms.repository.ContactRepository;
import com.cms.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for ContactServiceImpl.
 * Tests business logic for contact management including CRUD operations and search functionality.
 */
@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactServiceImpl contactService;

    private User testUser;
    private Contact testContact;
    private ContactRequest contactRequest;

    @BeforeEach
    void setUp() {
        // Initialize test data that will be reused in multiple test cases
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPasswordHash("hash123");
        testUser.setCreatedAt(LocalDateTime.now());

        testContact = new Contact();
        testContact.setId(1L);
        testContact.setUser(testUser);
        testContact.setFirstName("Jane");
        testContact.setLastName("Smith");
        testContact.setTitle("Dr");
        testContact.setEmail("jane@example.com");
        testContact.setPhone("5559876543");
        testContact.setAddress("456 Oak Ave");
        testContact.setCreatedAt(LocalDateTime.now());

        contactRequest = new ContactRequest();
        contactRequest.setUserId(1L);
        contactRequest.setFirstName("Jane");
        contactRequest.setLastName("Smith");
        contactRequest.setTitle("Dr");
        contactRequest.setEmail("jane@example.com");
        contactRequest.setPhone("5559876543");
        contactRequest.setAddress("456 Oak Ave");
    }

    /**
     * Test listing all contacts when no search is provided.
     */
    @Test
    void listContactsReturnsAllContactsWhenSearchIsEmpty() {
        List<Contact> contacts = new ArrayList<>();
        contacts.add(testContact);
        given(contactRepository.findAll(PageRequest.of(0, 1000))).willReturn(new PageImpl<>(contacts));

        List<ContactResponse> result = contactService.listContacts(null);

        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).getFirstName());
        verify(contactRepository).findAll(PageRequest.of(0, 1000));
    }

    /**
     * Test listing contacts with search filter applied.
     */
    @Test
    void listContactsFiltersResultsWhenSearchIsProvided() {
        List<Contact> contacts = new ArrayList<>();
        contacts.add(testContact);
        Page<Contact> page = new PageImpl<>(contacts, PageRequest.of(0, 1000), 1);
        given(contactRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Jane", "Jane", PageRequest.of(0, 1000)))
            .willReturn(page);

        List<ContactResponse> result = contactService.listContacts("Jane");

        assertEquals(1, result.size());
        assertEquals("Jane", result.get(0).getFirstName());
    }

    /**
     * Test explicit paginated listing for contacts.
     */
    @Test
    void listContactsPagedReturnsPage() {
        List<Contact> contacts = List.of(testContact);
        Page<Contact> page = new PageImpl<>(contacts, PageRequest.of(0, 10), 1);
        given(contactRepository.findAll(PageRequest.of(0, 10))).willReturn(page);

        Page<ContactResponse> result = contactService.listContactsPaged(0, 10, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("Jane", result.getContent().get(0).getFirstName());
    }

    /**
     * We saw one UI request send invalid pagination params during manual testing,
     * so this verifies the service now clamps those values safely.
     */
    @Test
    void listContactsPagedNormalizesInvalidPageAndSize() {
        List<Contact> contacts = List.of(testContact);
        Page<Contact> page = new PageImpl<>(contacts, PageRequest.of(0, 10), 1);
        given(contactRepository.findAll(PageRequest.of(0, 10))).willReturn(page);

        Page<ContactResponse> result = contactService.listContactsPaged(-2, 0, null);

        assertEquals(1, result.getContent().size());
        verify(contactRepository).findAll(PageRequest.of(0, 10));
    }

    /**
     * Test retrieving a contact by ID successfully.
     */
    @Test
    void getContactReturnsContactWhenFound() {
        given(contactRepository.findById(1L)).willReturn(Optional.of(testContact));

        ContactResponse result = contactService.getContact(1L);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals(1L, result.getId());
    }

    /**
     * Test that getContact throws exception when contact is not found.
     */
    @Test
    void getContactThrowsExceptionWhenNotFound() {
        given(contactRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contactService.getContact(99L));
    }

    /**
     * Test creating a new contact successfully.
     */
    @Test
    void createContactSavesAndReturnsContact() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(contactRepository.save(any(Contact.class))).willReturn(testContact);

        ContactResponse result = contactService.createContact(contactRequest);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals(1L, result.getUserId());
        verify(contactRepository).save(any(Contact.class));
    }

    /**
     * Test that createContact throws exception when user does not exist.
     */
    @Test
    void createContactThrowsExceptionWhenUserNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());
        contactRequest.setUserId(99L);

        assertThrows(ResourceNotFoundException.class, () -> contactService.createContact(contactRequest));
    }

    /**
     * Test updating an existing contact.
     */
    @Test
    void updateContactUpdatesAndReturnsContact() {
        given(contactRepository.findById(1L)).willReturn(Optional.of(testContact));
        given(contactRepository.save(any(Contact.class))).willReturn(testContact);

        ContactResponse result = contactService.updateContact(1L, contactRequest);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        verify(contactRepository).save(any(Contact.class));
    }

    /**
     * Test deleting an existing contact.
     */
    @Test
    void deleteContactDeletesWhenFound() {
        given(contactRepository.findById(1L)).willReturn(Optional.of(testContact));

        contactService.deleteContact(1L);

        verify(contactRepository).deleteById(1L);
    }

    /**
     * Test deleting a contact throws when not found.
     */
    @Test
    void deleteContactThrowsWhenNotFound() {
        given(contactRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> contactService.deleteContact(42L));
    }
}
