package com.cms.service;

import com.cms.dto.ImportContactsResponse;
import com.cms.entity.Contact;
import com.cms.entity.User;
import com.cms.repository.ContactRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContactImportServiceImpl.
 * Tests CSV parsing, validation, batch insertion, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class ContactImportServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    private ContactImportService contactImportService;
    private User testUser;

    @BeforeEach
    void setUp() {
        contactImportService = new ContactImportServiceImpl(contactRepository);
        
        // Create a test user for import context
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
    }

    @Test
    void testImportValidCsvWithSingleContact() {
        // Arrange: Create a simple CSV with one valid contact
        String csvContent = "firstName,lastName,email,phone,title,address\n" +
                           "John,Doe,john@example.com,555-1234,Manager,123 Main St";
        
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact contact = invocation.getArgument(0);
            contact.setId(1L); // Simulate database ID assignment
            return contact;
        });

        // Act: Import the CSV
        ImportContactsResponse response = contactImportService.importContactsFromCsv(csvContent, testUser);

        // Assert: Verify success
        assertEquals(1, response.getTotalProcessed(), "Should process 1 row");
        assertEquals(1, response.getSuccessCount(), "Should successfully import 1 contact");
        assertEquals(0, response.getFailureCount(), "Should have 0 failures");
        assertTrue(response.getErrors().isEmpty(), "Should have no errors");
        
        // Verify repository was called once
        verify(contactRepository, times(1)).save(any(Contact.class));
        
        // Verify the saved contact has correct data
        ArgumentCaptor<Contact> contactCaptor = ArgumentCaptor.forClass(Contact.class);
        verify(contactRepository).save(contactCaptor.capture());
        Contact savedContact = contactCaptor.getValue();
        
        assertEquals("John", savedContact.getFirstName());
        assertEquals("Doe", savedContact.getLastName());
        assertEquals("john@example.com", savedContact.getEmail());
        assertEquals("555-1234", savedContact.getPhone());
        assertEquals("Manager", savedContact.getTitle());
        assertEquals("123 Main St", savedContact.getAddress());
        assertEquals(testUser, savedContact.getUser());
    }

    @Test
    void testImportCsvWithFlexibleHeaderNames() {
        // Arrange: CSV with alternative column names (underscores, spaces)
        String csvContent = "first_name,last_name,email,phone\n" +
                           "Jane,Smith,jane@example.com,555-5678\n" +
                           "Bob,Johnson,bob@example.com,555-9999";
        
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact contact = invocation.getArgument(0);
            contact.setId(Math.random() > 0.5 ? 1L : 2L);
            return contact;
        });

        // Act
        ImportContactsResponse response = contactImportService.importContactsFromCsv(csvContent, testUser);

        // Assert: Both rows should import successfully despite header variations
        assertEquals(2, response.getTotalProcessed());
        assertEquals(2, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        
        verify(contactRepository, times(2)).save(any(Contact.class));
    }

    @Test
    void testImportCsvWithMissingFirstName() {
        // Arrange: CSV with a row missing firstName (required field)
        String csvContent = "firstName,lastName,email\n" +
                           "John,Doe,john@example.com\n" +
                           ",Smith,smith@example.com"; // Missing firstName
        
        when(contactRepository.save(any(Contact.class))).thenReturn(new Contact());

        // Act
        ImportContactsResponse response = contactImportService.importContactsFromCsv(csvContent, testUser);

        // Assert: First row succeeds, second fails
        assertEquals(2, response.getTotalProcessed());
        assertEquals(1, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());
        assertFalse(response.getErrors().isEmpty(), "Should have error for missing firstName");
        assertTrue(response.getErrors().get(0).contains("firstName is required"));
        
        // Only one contact should be saved
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void testImportEmptyCsv() {
        // Arrange: An empty or header-only CSV
        String csvContent = "firstName,lastName,email";
        
        // Act
        ImportContactsResponse response = contactImportService.importContactsFromCsv(csvContent, testUser);

        // Assert: No contacts imported
        assertEquals(0, response.getSuccessCount());
        assertFalse(response.getErrors().isEmpty(), "Should have error for empty data");
        
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void testImportCsvWithQuotedFields() {
        // Arrange: CSV with quoted field containing comma
        String csvContent = "firstName,lastName,email,address\n" +
                           "John,Doe,john@example.com,\"123 Main St, Suite 100\"";
        
        when(contactRepository.save(any(Contact.class))).thenReturn(new Contact());

        // Act
        ImportContactsResponse response = contactImportService.importContactsFromCsv(csvContent, testUser);

        // Assert: Address with comma should be handled correctly
        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        
        ArgumentCaptor<Contact> contactCaptor = ArgumentCaptor.forClass(Contact.class);
        verify(contactRepository).save(contactCaptor.capture());
        Contact savedContact = contactCaptor.getValue();
        
        // Address should preserve the comma inside quotes (without quotes)
        assertTrue(savedContact.getAddress().contains("123 Main St") && 
                   savedContact.getAddress().contains("Suite 100"));
    }

    @Test
    void testImportCsvWithPartialData() {
        // Arrange: CSV where some fields are optional and missing
        String csvContent = "firstName,lastName,email,phone,title,address\n" +
                           "Alice,Brown,alice@example.com,,CEO,\n" + // Phone and address missing
                           "Bob,Green,bob@example.com,555-7777,,456 Oak Ave"; // Title missing
        
        when(contactRepository.save(any(Contact.class))).thenReturn(new Contact());

        // Act
        ImportContactsResponse response = contactImportService.importContactsFromCsv(csvContent, testUser);

        // Assert: Both should succeed with empty optional fields
        assertEquals(2, response.getTotalProcessed());
        assertEquals(2, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        
        verify(contactRepository, times(2)).save(any(Contact.class));
    }

    @Test
    void testImportCsvWithWhitespaceHandling() {
        // Arrange: CSV with extra whitespace in data
        String csvContent = "firstName,lastName,email\n" +
                           "  John  ,  Doe  ,  john@example.com  ";
        
        when(contactRepository.save(any(Contact.class))).thenReturn(new Contact());

        // Act
        ImportContactsResponse response = contactImportService.importContactsFromCsv(csvContent, testUser);

        // Assert: Should trim whitespace
        assertEquals(1, response.getSuccessCount());
        
        ArgumentCaptor<Contact> contactCaptor = ArgumentCaptor.forClass(Contact.class);
        verify(contactRepository).save(contactCaptor.capture());
        Contact savedContact = contactCaptor.getValue();
        
        // Whitespace should be trimmed
        assertEquals("John", savedContact.getFirstName());
        assertEquals("Doe", savedContact.getLastName());
        assertEquals("john@example.com", savedContact.getEmail());
    }

    @Test
    void testImportCsvWithEmptyLines() {
        // Arrange: CSV with empty lines in the middle
        String csvContent = "firstName,lastName,email\n" +
                           "John,Doe,john@example.com\n" +
                           "\n" +
                           "Jane,Smith,jane@example.com";
        
        when(contactRepository.save(any(Contact.class))).thenReturn(new Contact());

        // Act
        ImportContactsResponse response = contactImportService.importContactsFromCsv(csvContent, testUser);

        // Assert: Should skip empty lines and process valid ones
        assertEquals(2, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        
        verify(contactRepository, times(2)).save(any(Contact.class));
    }

    @Test
    void testImportLargeCSV() {
        // Arrange: Create a CSV with many contacts (but under the 1000 limit)
        StringBuilder csvContent = new StringBuilder("firstName,lastName,email%n");
        int contactCount = 100;
        for (int i = 0; i < contactCount; i++) {
            csvContent.append(String.format("Contact%d,User%d,contact%d@example.com%n", i, i, i));
        }
        
        when(contactRepository.save(any(Contact.class))).thenReturn(new Contact());

        // Act
        ImportContactsResponse response = contactImportService.importContactsFromCsv(csvContent.toString(), testUser);

        // Assert: All should be imported
        assertEquals(contactCount, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        
        verify(contactRepository, times(contactCount)).save(any(Contact.class));
    }

    @Test
    void testImportMessageFormatting() {
        // Arrange
        String csvContent = "firstName,lastName\n" +
                           "John,Doe\n" +
                           "Jane,Smith";
        
        when(contactRepository.save(any(Contact.class))).thenReturn(new Contact());

        // Act
        ImportContactsResponse response = contactImportService.importContactsFromCsv(csvContent, testUser);

        // Assert: Response message should summarize the operation
        assertTrue(response.getMessage().contains("2 succeeded"));
        assertTrue(response.getMessage().contains("0 failed"));
        assertTrue(response.getMessage().contains("2 total rows"));
    }

    @Test
    void testImportSetsBothSuccessAndFailure() {
        // Arrange: Mixed success and failure scenario
        String csvContent = "firstName,lastName,email\n" +
                           "John,Doe,john@example.com\n" +
                           ",Smith,smith@example.com\n" + // Missing firstName
                           "Bob,Johnson,bob@example.com";
        
        when(contactRepository.save(any(Contact.class))).thenReturn(new Contact());

        // Act
        ImportContactsResponse response = contactImportService.importContactsFromCsv(csvContent, testUser);

        // Assert: Should have both successes and failures
        assertEquals(3, response.getTotalProcessed());
        assertEquals(2, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());
        assertEquals(1, response.getErrors().size());
        
        verify(contactRepository, times(2)).save(any(Contact.class));
    }
}
