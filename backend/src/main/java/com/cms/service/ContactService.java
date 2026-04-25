package com.cms.service;

import com.cms.dto.ContactRequest;
import com.cms.dto.ContactResponse;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Service interface for contact management operations.
 * Defines contracts for CRUD operations on contacts.
 */
public interface ContactService {

    /**
     * List all contacts with optional search filter.
     *
     * @param search optional search term to filter by first or last name
     * @return list of matching contacts
     */
    List<ContactResponse> listContacts(String search);

    /**
     * List contacts with pagination and optional search.
     */
    Page<ContactResponse> listContactsPaged(int page, int size, String search);

    /**
     * Retrieve a specific contact by ID.
     *
     * @param id the contact's ID
     * @return the contact details
     * @throws ResourceNotFoundException if contact not found
     */
    ContactResponse getContact(Long id);

    /**
     * Create a new contact.
     *
     * @param request contact data to create
     * @return the newly created contact
     */
    ContactResponse createContact(ContactRequest request);

    /**
     * Update an existing contact.
     *
     * @param id the contact's ID to update
     * @param request updated contact data
     * @return the updated contact
     * @throws ResourceNotFoundException if contact not found
     */
    ContactResponse updateContact(Long id, ContactRequest request);

    /**
     * Delete a contact by ID.
     *
     * @param id the contact's ID to delete
     * @throws ResourceNotFoundException if contact not found
     */
    void deleteContact(Long id);
}