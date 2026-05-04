package com.cms.service;

import com.cms.dto.BatchDeleteRequest;
import com.cms.dto.BatchOperationResponse;
import com.cms.entity.Contact;
import com.cms.entity.User;
import com.cms.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BatchContactService - bulk contact operations.
 * Tests batch delete and CSV export functionality with access control.
 */
@DisplayName("BatchContactService Tests")
class BatchContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactShareService contactShareService;

    @InjectMocks
    private BatchContactService batchContactService;

    private User currentUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test users
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("user@example.com");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");
    }

    @Test
    @DisplayName("Should delete multiple contacts in batch")
    void testDeleteContactsBatch() {
        // Arrange
        Contact contact1 = createTestContact(1L, "John", currentUser);
        Contact contact2 = createTestContact(2L, "Jane", currentUser);

        List<Long> contactIds = Arrays.asList(1L, 2L);
        BatchDeleteRequest request = new BatchDeleteRequest(contactIds);

        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact1));
        when(contactRepository.findById(2L)).thenReturn(Optional.of(contact2));

        // Act
        BatchOperationResponse response = batchContactService.deleteContactsBatch(request, currentUser);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        assertTrue(response.isAllSuccessful());
        assertEquals("delete", response.getOperationType());
        verify(contactRepository, times(2)).delete(any(Contact.class));
        verify(contactShareService, times(2)).revokeAllSharesForContact(any());
    }

    @Test
    @DisplayName("Should handle partial batch delete failures")
    void testDeleteContactsBatchWithFailures() {
        // Arrange
        Contact contact1 = createTestContact(1L, "John", currentUser);

        List<Long> contactIds = Arrays.asList(1L, 999L); // 999 doesn't exist
        BatchDeleteRequest request = new BatchDeleteRequest(contactIds);

        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact1));
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        BatchOperationResponse response = batchContactService.deleteContactsBatch(request, currentUser);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());
        assertFalse(response.isAllSuccessful());
        assertTrue(response.hasAnySuccess());
    }

    @Test
    @DisplayName("Should export contacts to CSV with proper formatting")
    void testExportContactsToCSV() {
        // Arrange
        Contact contact = createTestContact(1L, "John", currentUser);
        contact.setEmail("john@example.com");
        contact.setPhone("1555123456");

        List<Long> contactIds = Arrays.asList(1L);

        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        // Act
        String csv = batchContactService.exportContactsToCSV(contactIds, currentUser);

        // Assert
        assertNotNull(csv);
        assertTrue(csv.contains("ID,FirstName,LastName,Title")); // Check header
        assertTrue(csv.contains("John")); // Check data
        assertTrue(csv.contains("john@example.com")); // Check email
    }

    @Test
    @DisplayName("Should escape CSV fields with commas")
    void testCSVEscaping() {
        // Arrange
        Contact contact = new Contact();
        contact.setId(1L);
        contact.setFirstName("John");
        contact.setLastName("O'Brien, Jr.");
        contact.setUser(currentUser);

        List<Long> contactIds = Arrays.asList(1L);

        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        // Act
        String csv = batchContactService.exportContactsToCSV(contactIds, currentUser);

        // Assert
        assertNotNull(csv);
        // Field with comma should be quoted
        assertTrue(csv.contains("\"O'Brien, Jr.\""));
    }

    @Test
    @DisplayName("Should respect user ownership in batch delete")
    void testBatchDeleteRespectOwnership() {
        // Arrange
        Contact contact = createTestContact(1L, "John", otherUser); // Owned by otherUser
        contact.setUser(otherUser);

        List<Long> contactIds = Arrays.asList(1L);
        BatchDeleteRequest request = new BatchDeleteRequest(contactIds);

        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        // Act
        BatchOperationResponse response = batchContactService.deleteContactsBatch(request, currentUser);

        // Assert
        assertEquals(0, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());
        verify(contactRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should handle empty batch request gracefully")
    void testEmptyBatchDelete() {
        // Arrange
        BatchDeleteRequest request = new BatchDeleteRequest(Arrays.asList());

        // Act
        BatchOperationResponse response = batchContactService.deleteContactsBatch(request, currentUser);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        verify(contactRepository, never()).delete(any());
    }

    // Helper method to create test contacts
    private Contact createTestContact(Long id, String firstName, User user) {
        Contact contact = new Contact();
        contact.setId(id);
        contact.setFirstName(firstName);
        contact.setLastName("TestLast");
        contact.setUser(user);
        return contact;
    }
}
