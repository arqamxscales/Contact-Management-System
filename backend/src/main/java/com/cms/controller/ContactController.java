package com.cms.controller;

import com.cms.dto.ContactRequest;
import com.cms.dto.ContactResponse;
import com.cms.service.ContactService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing contact operations.
 * Handles HTTP requests for listing, retrieving, and creating contacts.
 * All endpoints are prefixed with /api/contacts
 */
@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    // Logger for tracking API calls and debugging
    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * Retrieves all contacts, with optional filtering by search term.
     * GET /api/contacts?search=John
     *
     * @param search optional search parameter to filter contacts by first or last name
     * @return list of contacts matching the search criteria
     */
    @GetMapping
    public ResponseEntity<List<ContactResponse>> listContacts(@RequestParam(value = "search", required = false) String search) {
        log.debug("Listing contacts with search={}", search);
        return ResponseEntity.ok(contactService.listContacts(search));
    }

    /**
     * Retrieves contacts in a paginated format.
     * GET /api/contacts/paged?page=0&size=10&search=John
     */
    @GetMapping("/paged")
    public ResponseEntity<Page<ContactResponse>> listContactsPaged(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "search", required = false) String search
    ) {
        log.debug("Listing contacts page={}, size={}, search={}", page, size, search);
        return ResponseEntity.ok(contactService.listContactsPaged(page, size, search));
    }

    /**
     * Retrieves a specific contact by its ID.
     * GET /api/contacts/{id}
     *
     * @param id the contact ID to retrieve
     * @return the contact details if found, otherwise 404 error
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContact(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.getContact(id));
    }

    /**
     * Creates a new contact with validated data.
     * POST /api/contacts
     *
     * @param request the contact data (firstName is required)
     * @return the created contact with generated ID and 201 status
     */
    @PostMapping
    public ResponseEntity<ContactResponse> createContact(@Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.createContact(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing contact with new information.
     * PUT /api/contacts/{id}
     *
     * @param id the ID of the contact to update
     * @param request the updated contact data
     * @return the updated contact information
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(@PathVariable Long id, @Valid @RequestBody ContactRequest request) {
        log.debug("Updating contact with id={}", id);
        ContactResponse response = contactService.updateContact(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a contact by ID.
     * DELETE /api/contacts/{id}
     *
     * @param id the ID of the contact to delete
     * @return 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        log.debug("Deleting contact with id={}", id);
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }

    // Small maintenance note: keep this class closing brace intact.
    // It got dropped once during a squash and wasted debugging time.
}