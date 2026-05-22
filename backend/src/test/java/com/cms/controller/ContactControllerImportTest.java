package com.cms.controller;

import com.cms.dto.ImportContactsResponse;
import com.cms.dto.UserResponse;
import com.cms.entity.User;
import com.cms.service.BatchContactService;
import com.cms.service.ContactImportService;
import com.cms.service.ContactService;
import com.cms.service.UserService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContactController's import endpoint.
 * Tests the HTTP layer, file handling, and error responses.
 * Also verifies proper user context passing and response structure.
 */
@ExtendWith(MockitoExtension.class)
class ContactControllerImportTest {

    private static final String CSV_FILENAME = "contacts.csv";
    private static final String CSV_MEDIA_TYPE = "text/csv";

    @Mock
    private ContactService contactService;

    @Mock
    private BatchContactService batchContactService;

    @Mock
    private ContactImportService contactImportService;

    @Mock
    private UserService userService;

    private ContactController contactController;
    private User testUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        contactController = new ContactController(
            contactService,
            batchContactService,
            contactImportService,
            userService
        );

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setFullName("Test User");
        userResponse.setEmail("test@example.com");
    }

    @Test
    void testImportContactsWithValidFile() throws Exception {
        // Arrange: Create a valid CSV file
        String csvContent = "firstName,lastName,email\nJohn,Doe,john@example.com";
        MultipartFile file = new MockMultipartFile(
            "file",
            CSV_FILENAME,
            CSV_MEDIA_TYPE,
            csvContent.getBytes(StandardCharsets.UTF_8)
        );

        // Setup mocks
        when(userService.getUserProfile(1L)).thenReturn(userResponse);
        
        List<String> emptyErrors = new ArrayList<>();
        ImportContactsResponse importResponse = new ImportContactsResponse(
            1, 1, 0, emptyErrors, "Import completed: 1 succeeded, 0 failed out of 1 total rows"
        );
        when(contactImportService.importContactsFromCsv(csvContent, testUser))
            .thenReturn(importResponse);

        // Act: Call the import endpoint
        ResponseEntity<ImportContactsResponse> response = contactController.importContacts(1L, file);

        // Assert: Should return 200 OK with success details
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getSuccessCount());
        assertEquals(0, response.getBody().getFailureCount());
        assertEquals(1, response.getBody().getTotalProcessed());

        // Verify service was called
        verify(userService, times(1)).getUserProfile(1L);
        verify(contactImportService, times(1)).importContactsFromCsv(
            eq(csvContent),
            any(User.class)
        );
    }

    @Test
    void testImportContactsWithPartialFailure() {
        // Arrange: CSV with some invalid rows
        String csvContent = "firstName,lastName\nJohn,Doe\n,Smith\nBob,Johnson";
        MultipartFile file = new MockMultipartFile(
            "file",
            CSV_FILENAME,
            CSV_MEDIA_TYPE,
            csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userService.getUserProfile(1L)).thenReturn(userResponse);
        
        // Mock service response with partial failures
        List<String> errors = List.of("Row 3: firstName is required");
        ImportContactsResponse importResponse = new ImportContactsResponse(
            3, 2, 1, errors, "Import completed: 2 succeeded, 1 failed out of 3 total rows"
        );
        when(contactImportService.importContactsFromCsv(csvContent, testUser))
            .thenReturn(importResponse);

        // Act
        ResponseEntity<ImportContactsResponse> response = contactController.importContacts(1L, file);

        // Assert: Should report partial success
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getSuccessCount());
        assertEquals(1, response.getBody().getFailureCount());
        assertEquals(1, response.getBody().getErrors().size());
    }

    @Test
    void testImportContactsFileReadingFailure() throws Exception {
        // Arrange: Create a file that throws an exception when reading bytes
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new RuntimeException("File read error"));
        when(file.getOriginalFilename()).thenReturn(CSV_FILENAME);

        when(userService.getUserProfile(1L)).thenReturn(userResponse);

        // Act
        ResponseEntity<ImportContactsResponse> response = contactController.importContacts(1L, file);

        // Assert: Should return 400 Bad Request on file read error
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(1, response.getBody().getFailureCount());
        assertTrue(response.getBody().getMessage().contains("File upload failed"));
    }

    @Test
    void testImportContactsEmptyFile() {
        // Arrange: Empty CSV file
        String csvContent = "";
        MultipartFile file = new MockMultipartFile(
            "file",
            CSV_FILENAME,
            CSV_MEDIA_TYPE,
            csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userService.getUserProfile(1L)).thenReturn(userResponse);
        
        // Service returns error for empty file
        List<String> errors = List.of("CSV file appears to be empty or missing data rows");
        ImportContactsResponse importResponse = new ImportContactsResponse(
            0, 0, 1, errors, "No contacts to import"
        );
        when(contactImportService.importContactsFromCsv(csvContent, testUser))
            .thenReturn(importResponse);

        // Act
        ResponseEntity<ImportContactsResponse> response = contactController.importContacts(1L, file);

        // Assert: Should return 200 but with failure details
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getSuccessCount());
        assertEquals(1, response.getBody().getFailureCount());
    }

    @Test
    void testImportContactsCallsUserServiceWithCorrectId() {
        // Arrange
        String csvContent = "firstName,lastName\nAlice,Wonder";
        MultipartFile file = new MockMultipartFile(
            "file",
            CSV_FILENAME,
            CSV_MEDIA_TYPE,
            csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userService.getUserProfile(1L)).thenReturn(userResponse);
        
        ImportContactsResponse importResponse = new ImportContactsResponse(
            1, 1, 0, new ArrayList<>(), "Success"
        );
        when(contactImportService.importContactsFromCsv(csvContent, testUser))
            .thenReturn(importResponse);

        // Act
        contactController.importContacts(1L, file);

        // Assert: User service should be called with correct ID
        verify(userService).getUserProfile(1L);
    }

    @Test
    void testImportContactsResponseHasAllRequiredFields() {
        // Arrange
        String csvContent = "firstName,lastName\nJohn,Doe";
        MultipartFile file = new MockMultipartFile(
            "file",
            CSV_FILENAME,
            CSV_MEDIA_TYPE,
            csvContent.getBytes(StandardCharsets.UTF_8)
        );

        when(userService.getUserProfile(1L)).thenReturn(userResponse);
        
        List<String> errors = new ArrayList<>();
        ImportContactsResponse importResponse = new ImportContactsResponse(
            1, 1, 0, errors, "Test message"
        );
        when(contactImportService.importContactsFromCsv(csvContent, testUser))
            .thenReturn(importResponse);

        // Act
        ResponseEntity<ImportContactsResponse> response = contactController.importContacts(1L, file);

        // Assert: Response should have all required fields
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getTotalProcessed() >= 0);
        assertTrue(response.getBody().getSuccessCount() >= 0);
        assertTrue(response.getBody().getFailureCount() >= 0);
        assertNotNull(response.getBody().getErrors());
        assertNotNull(response.getBody().getMessage());
    }

    @Test
    void testImportContactsLargeFileProcess() {
        // Arrange: Build a CSV with 500 contacts to test batch handling
        StringBuilder csvBuilder = new StringBuilder(String.format("firstName,lastName%n"));
        for (int i = 0; i < 500; i++) {
            csvBuilder.append(String.format("Contact%d,User%d%n", i, i));
        }
        
        MultipartFile file = new MockMultipartFile(
            "file",
            CSV_FILENAME,
            CSV_MEDIA_TYPE,
            csvBuilder.toString().getBytes(StandardCharsets.UTF_8)
        );

        when(userService.getUserProfile(1L)).thenReturn(userResponse);
        
        ImportContactsResponse importResponse = new ImportContactsResponse(
            500, 500, 0, new ArrayList<>(), 
            "Import completed: 500 succeeded, 0 failed"
        );
        when(contactImportService.importContactsFromCsv(
            csvBuilder.toString(), testUser))
            .thenReturn(importResponse);

        // Act
        ResponseEntity<ImportContactsResponse> response = contactController.importContacts(1L, file);

        // Assert: Should handle large files
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(500, response.getBody().getSuccessCount());
    }
}
