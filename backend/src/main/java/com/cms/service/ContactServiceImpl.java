package com.cms.service;

import com.cms.dto.ContactRequest;
import com.cms.dto.ContactResponse;
import com.cms.entity.Contact;
import com.cms.entity.User;
import com.cms.exception.ResourceNotFoundException;
import com.cms.repository.ContactRepository;
import com.cms.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Implementation of the ContactService interface.
 * Handles all business logic related to contact management including
 * listing, searching, creating, and managing contacts.
 */
@Service
public class ContactServiceImpl implements ContactService {

    // Logger for tracking service operations
    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactServiceImpl(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponse> listContacts(String search) {
        // Backward-compatible method used by existing endpoints/tests.
        return listContactsPaged(0, 1000, search).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> listContactsPaged(int page, int size, String search) {
        // Small guardrail from yesterday's pagination work:
        // if UI sends weird values, we still return a sane page instead of blowing up.
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Contact> contactPage;

        if (StringUtils.hasText(search)) {
            contactPage = contactRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(search, search, pageable);
        } else {
            contactPage = contactRepository.findAll(pageable);
        }

        List<ContactResponse> mapped = contactPage.getContent().stream().map(this::toResponse).collect(Collectors.toList());
        return new PageImpl<>(mapped, pageable, contactPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponse getContact(Long id) {
        // Retrieve contact by ID, throw exception if not found
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id " + id));
        return toResponse(contact);
    }

    @Override
    @Transactional
    public ContactResponse createContact(ContactRequest request) {
        // Verify that the user exists before creating a contact
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + request.getUserId()));

        // Create a new contact entity and populate it with request data
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setAddress(request.getAddress());
        contact.setCreatedAt(LocalDateTime.now());

        // Save the contact to the database
        Contact savedContact = contactRepository.save(contact);
        log.info("Created contact {} for user {}", savedContact.getId(), user.getId());
        return toResponse(savedContact);
    }

    /**
     * Converts a Contact entity to a ContactResponse DTO.
     * This method handles null safety for the user relationship.
     */
    private ContactResponse toResponse(Contact contact) {
        ContactResponse response = new ContactResponse();
        response.setId(contact.getId());
        response.setUserId(Objects.nonNull(contact.getUser()) ? contact.getUser().getId() : null);
        response.setFirstName(contact.getFirstName());
        response.setLastName(contact.getLastName());
        response.setTitle(contact.getTitle());
        response.setEmail(contact.getEmail());
        response.setPhone(contact.getPhone());
        response.setAddress(contact.getAddress());
        response.setCreatedAt(contact.getCreatedAt());
        return response;
    }

    @Override
    @Transactional
    public ContactResponse updateContact(Long id, ContactRequest request) {
        // Find the contact to update, throw exception if not found
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id " + id));

        // Update contact fields with new values from request
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setAddress(request.getAddress());

        // Save the updated contact to database
        Contact updatedContact = contactRepository.save(contact);
        log.info("Updated contact {} for user {}", updatedContact.getId(), contact.getUser().getId());
        return toResponse(updatedContact);
    }

    @Override
    @Transactional
    public void deleteContact(Long id) {
        // Verify contact exists before deleting
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id " + id));

        // Delete the contact from database
        contactRepository.deleteById(id);
        log.info("Deleted contact {} for user {}", id, contact.getUser().getId());
    }
}