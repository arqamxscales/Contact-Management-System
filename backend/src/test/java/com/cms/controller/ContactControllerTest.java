package com.cms.controller;

import com.cms.dto.ContactRequest;
import com.cms.dto.ContactResponse;
import com.cms.exception.ResourceNotFoundException;
import com.cms.service.ContactService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the ContactController.
 * Tests REST endpoints for listing, retrieving, and creating contacts.
 */
@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    /**
     * Test that listContacts endpoint returns contacts matching search criteria.
     */
    @Test
    void listContactsReturnsContacts() throws Exception {
        ContactResponse response = createResponse();
        // Mock the service to return a contact when searching for "Sam"
        given(contactService.listContacts(eq("Sam"))).willReturn(List.of(response));

        // Execute GET request with search parameter
        mockMvc.perform(get("/api/contacts").param("search", "Sam"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("Sam"))
            .andExpect(jsonPath("$[0].title").value("Mr"));
    }

    /**
     * Test that listContacts returns all contacts when no search is provided.
     */
    @Test
    void listContactsReturnsAllWhenNoSearchProvided() throws Exception {
        ContactResponse response = createResponse();
        given(contactService.listContacts(eq(null))).willReturn(List.of(response));

        mockMvc.perform(get("/api/contacts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("Sam"));
    }

    /**
     * Test that createContact endpoint creates a new contact with valid data.
     */
    @Test
    void createContactReturnsCreatedContact() throws Exception {
        ContactResponse response = createResponse();
        // Mock the service to return the created contact
        given(contactService.createContact(any(ContactRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": 1,
                      "firstName": "Sam",
                      "lastName": "Lee",
                      "title": "Mr",
                      "email": "sam@example.com",
                      "phone": "5551234567",
                      "address": "123 Main Street"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("Sam"))
            .andExpect(jsonPath("$.userId").value(1));
    }

    /**
     * Test that createContact fails validation when firstName is missing.
     */
    @Test
    void createContactFailsWithoutFirstName() throws Exception {
        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": 1,
                      "lastName": "Lee"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    /**
     * Test paged endpoint returns expected structure.
     */
    @Test
    void listContactsPagedReturnsPage() throws Exception {
        ContactResponse response = createResponse();
        given(contactService.listContactsPaged(0, 10, "Sam"))
            .willReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/contacts/paged").param("page", "0").param("size", "10").param("search", "Sam"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].firstName").value("Sam"));
    }

    /**
     * Test update endpoint returns updated contact.
     */
    @Test
    void updateContactReturnsUpdatedContact() throws Exception {
        ContactResponse response = createResponse();
        given(contactService.updateContact(eq(10L), any(ContactRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/contacts/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": 1,
                      "firstName": "Sam",
                      "lastName": "Lee",
                      "title": "Mr"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10));
    }

    /**
     * Test delete endpoint returns 204.
     */
    @Test
    void deleteContactReturnsNoContent() throws Exception {
        doNothing().when(contactService).deleteContact(10L);

        mockMvc.perform(delete("/api/contacts/10"))
            .andExpect(status().isNoContent());
    }

    /**
     * Task 9 follow-up: ensure missing contacts are translated into a clean 404 API error.
     */
    @Test
    void getContactReturns404WhenContactDoesNotExist() throws Exception {
        given(contactService.getContact(999L))
            .willThrow(new ResourceNotFoundException("Contact not found with id 999"));

        mockMvc.perform(get("/api/contacts/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Contact not found with id 999"))
            .andExpect(jsonPath("$.path").value("/api/contacts/999"));
    }

    /**
     * Helper method to create a sample ContactResponse for testing.
     */
    private ContactResponse createResponse() {
        ContactResponse response = new ContactResponse();
        response.setId(10L);
        response.setUserId(1L);
        response.setFirstName("Sam");
        response.setLastName("Lee");
        response.setTitle("Mr");
        response.setEmail("sam@example.com");
        response.setPhone("5551234567");
        response.setAddress("123 Main Street");
        response.setCreatedAt(LocalDateTime.of(2026, 4, 24, 10, 0));
        return response;
    }
}