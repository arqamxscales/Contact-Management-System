package com.cms.controller;

import com.cms.dto.BatchOperationResponse;
import com.cms.dto.ContactRequest;
import com.cms.dto.ContactResponse;
import com.cms.dto.UserResponse;
import com.cms.exception.ResourceNotFoundException;
import com.cms.service.BatchContactService;
import com.cms.service.ContactService;
import com.cms.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the ContactController.
 * Covers the core contact routes plus the new batch delete/export flow.
 */
@WebMvcTest(ContactController.class)
class ContactControllerTest {

    private static final String CONTACTS_PATH = "/api/contacts";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @MockBean
    private BatchContactService batchContactService;

    @MockBean
    private UserService userService;

    @Test
    void listContactsReturnsContacts() throws Exception {
        ContactResponse response = createResponse();
        given(contactService.listContacts(eq("Sam"))).willReturn(List.of(response));

        mockMvc.perform(get(CONTACTS_PATH).param("search", "Sam"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("Sam"))
            .andExpect(jsonPath("$[0].title").value("Mr"));
    }

    @Test
    void listContactsReturnsAllWhenNoSearchProvided() throws Exception {
        ContactResponse response = createResponse();
        given(contactService.listContacts(eq(null))).willReturn(List.of(response));

        mockMvc.perform(get(CONTACTS_PATH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("Sam"));
    }

    @Test
    void createContactReturnsCreatedContact() throws Exception {
        ContactResponse response = createResponse();
        given(contactService.createContact(any(ContactRequest.class))).willReturn(response);

        mockMvc.perform(post(CONTACTS_PATH)
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

    @Test
    void createContactFailsWithoutFirstName() throws Exception {
        mockMvc.perform(post(CONTACTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": 1,
                      "lastName": "Lee"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createContactFailsWithInvalidEmailFormat() throws Exception {
        mockMvc.perform(post(CONTACTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": 1,
                      "firstName": "Sam",
                      "email": "not-an-email"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void listContactsPagedReturnsPage() throws Exception {
        ContactResponse response = createResponse();
        given(contactService.listContactsPaged(0, 10, "Sam"))
            .willReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get(CONTACTS_PATH + "/paged").param("page", "0").param("size", "10").param("search", "Sam"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].firstName").value("Sam"));
    }

    @Test
    void updateContactReturnsUpdatedContact() throws Exception {
        ContactResponse response = createResponse();
        given(contactService.updateContact(eq(10L), any(ContactRequest.class))).willReturn(response);

        mockMvc.perform(put(CONTACTS_PATH + "/10")
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

    @Test
    void deleteContactReturnsNoContent() throws Exception {
        doNothing().when(contactService).deleteContact(10L);

        mockMvc.perform(delete(CONTACTS_PATH + "/10"))
            .andExpect(status().isNoContent());
    }

    @Test
    void getContactReturns404WhenContactDoesNotExist() throws Exception {
        given(contactService.getContact(999L))
            .willThrow(new ResourceNotFoundException("Contact not found with id 999"));

        mockMvc.perform(get(CONTACTS_PATH + "/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Contact not found with id 999"))
            .andExpect(jsonPath("$.path").value(CONTACTS_PATH + "/999"));
    }

    @Test
    void createContactReturns400WhenEmailAlreadyExistsForUser() throws Exception {
        given(contactService.createContact(any(ContactRequest.class)))
            .willThrow(new IllegalArgumentException("A contact with this email already exists for the user"));

        mockMvc.perform(post(CONTACTS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": 1,
                      "firstName": "Sam",
                      "email": "sam@example.com"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("A contact with this email already exists for the user"))
            .andExpect(jsonPath("$.path").value(CONTACTS_PATH));
    }

    @Test
    void deleteContactsBatchReturnsSummary() throws Exception {
        BatchOperationResponse response = new BatchOperationResponse(2, 2, 0, "delete", "Deleted 2 of 2 contacts");
        given(batchContactService.deleteContactsBatch(any(), any())).willReturn(response);
        given(userService.getUserProfile(1L)).willReturn(createUserResponse());

        mockMvc.perform(post(CONTACTS_PATH + "/batch-delete")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "contactIds": [1, 2],
                      "requireConfirmation": true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.successCount").value(2))
            .andExpect(jsonPath("$.operationType").value("delete"));
    }

    @Test
    void exportContactsReturnsCsvDownload() throws Exception {
        given(batchContactService.exportContactsToCSV(List.of(1L, 2L), any()))
            .willReturn("ID,FirstName\n1,Sam\n2,Jane");
        given(userService.getUserProfile(1L)).willReturn(createUserResponse());

        mockMvc.perform(post(CONTACTS_PATH + "/export")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "contactIds": [1, 2]
                    }
                    """))
            .andExpect(status().isOk());
    }

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

    private UserResponse createUserResponse() {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setFullName("John Doe");
        response.setEmail("john@example.com");
        response.setPhone("5551234567");
        response.setCreatedAt(LocalDateTime.of(2026, 4, 24, 10, 0));
        return response;
    }
}