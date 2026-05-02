package com.cms.service;

import com.cms.entity.Contact;
import com.cms.entity.ContactShare;
import com.cms.entity.User;
import com.cms.exception.ResourceNotFoundException;
import com.cms.repository.ContactShareRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContactShareService.
 * Tests sharing logic, permission management, and access control.
 */
@DisplayName("ContactShareService Tests")
class ContactShareServiceTest {

    @Mock
    private ContactShareRepository contactShareRepository;

    @InjectMocks
    private ContactShareService contactShareService;

    private User owner;
    private User sharedWith;
    private Contact contact;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        sharedWith = new User();
        sharedWith.setId(2L);
        sharedWith.setEmail("shared@example.com");

        contact = new Contact();
        contact.setId(1L);
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setUser(owner);
    }

    @Test
    @DisplayName("Should share contact with another user")
    void testShareContact() {
        // Arrange
        when(contactShareRepository.existsByContactIdAndSharedWithId(1L, 2L)).thenReturn(false);
        when(contactShareRepository.save(any(ContactShare.class))).thenAnswer(invocation -> {
            ContactShare share = invocation.getArgument(0);
            share.setId(1L);
            return share;
        });

        // Act
        ContactShare result = contactShareService.shareContact(contact, owner, sharedWith, "read");

        // Assert
        assertNotNull(result);
        assertEquals("read", result.getPermission());
        verify(contactShareRepository, times(1)).save(any(ContactShare.class));
    }

    @Test
    @DisplayName("Should not allow self-sharing")
    void testShareContactWithSelf() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            contactShareService.shareContact(contact, owner, owner, "read");
        });
    }

    @Test
    @DisplayName("Should update permission if already shared")
    void testUpdateSharePermission() {
        // Arrange
        ContactShare existingShare = new ContactShare(contact, owner, sharedWith, "read");
        existingShare.setId(1L);
        when(contactShareRepository.existsByContactIdAndSharedWithId(1L, 2L)).thenReturn(true);
        when(contactShareRepository.findByContactIdAndSharedWithId(1L, 2L))
            .thenReturn(Optional.of(existingShare));
        when(contactShareRepository.save(any(ContactShare.class))).thenReturn(existingShare);

        // Act
        ContactShare result = contactShareService.shareContact(contact, owner, sharedWith, "write");

        // Assert
        assertEquals("write", result.getPermission());
        verify(contactShareRepository, times(1)).save(any(ContactShare.class));
    }

    @Test
    @DisplayName("Should revoke share")
    void testRevokeShare() {
        // Arrange
        ContactShare share = new ContactShare(contact, owner, sharedWith, "read");
        share.setId(1L);
        when(contactShareRepository.findByContactIdAndSharedWithId(1L, 2L))
            .thenReturn(Optional.of(share));

        // Act
        contactShareService.revokeShare(1L, 2L);

        // Assert
        verify(contactShareRepository, times(1)).delete(share);
    }

    @Test
    @DisplayName("Owner always has access")
    void testOwnerHasAccess() {
        // Act
        boolean hasAccess = contactShareService.hasAccess(contact, owner);

        // Assert
        assertTrue(hasAccess);
    }

    @Test
    @DisplayName("Non-owner user has access only if shared")
    void testNonOwnerAccessCheck() {
        // Arrange
        when(contactShareRepository.existsByContactIdAndSharedWithId(1L, 2L)).thenReturn(true);

        // Act
        boolean hasAccess = contactShareService.hasAccess(contact, sharedWith);

        // Assert
        assertTrue(hasAccess);
    }

    @Test
    @DisplayName("Owner has write access")
    void testOwnerHasWriteAccess() {
        // Act
        boolean hasWriteAccess = contactShareService.hasWriteAccess(contact, owner);

        // Assert
        assertTrue(hasWriteAccess);
    }

    @Test
    @DisplayName("Shared user has write access only with write permission")
    void testSharedUserWriteAccess() {
        // Arrange
        ContactShare share = new ContactShare(contact, owner, sharedWith, "write");
        share.setId(1L);
        when(contactShareRepository.findByContactIdAndSharedWithId(1L, 2L))
            .thenReturn(Optional.of(share));

        // Act
        boolean hasWriteAccess = contactShareService.hasWriteAccess(contact, sharedWith);

        // Assert
        assertTrue(hasWriteAccess);
    }

    @Test
    @DisplayName("Shared user with read permission does not have write access")
    void testSharedUserReadOnlyAccess() {
        // Arrange
        ContactShare share = new ContactShare(contact, owner, sharedWith, "read");
        share.setId(1L);
        when(contactShareRepository.findByContactIdAndSharedWithId(1L, 2L))
            .thenReturn(Optional.of(share));

        // Act
        boolean hasWriteAccess = contactShareService.hasWriteAccess(contact, sharedWith);

        // Assert
        assertFalse(hasWriteAccess);
    }
}
