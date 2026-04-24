package com.cms.controller;

import com.cms.dto.ContactRequest;
import com.cms.dto.ContactResponse;
import com.cms.service.ContactService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Test
    void listContactsReturnsContacts() throws Exception {
        ContactResponse response = createResponse();
        given(contactService.listContacts(eq("Sam"))).willReturn(List.of(response));

        mockMvc.perform(get("/api/contacts").param("search", "Sam"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("Sam"))
            .andExpect(jsonPath("$[0].title").value("Mr"));
    }

    @Test
    void createContactReturnsCreatedContact() throws Exception {
        ContactResponse response = createResponse();
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