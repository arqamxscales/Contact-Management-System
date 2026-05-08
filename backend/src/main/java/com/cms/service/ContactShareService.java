package com.cms.service;

import com.cms.entity.Contact;
import com.cms.entity.ContactShare;
import com.cms.entity.User;
import com.cms.exception.ResourceNotFoundException;
import com.cms.repository.ContactShareRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing contact sharing and access control.
 * Handles share creation, updates, and permission checks.
 * Logs all sharing operations for audit purposes.
 */
@Service
@Slf4j
public class ContactShareService {

    @Autowired
    private ContactShareRepository contactShareRepository;

    /**
     * Share a contact with another user.
     * Logs the share action for audit trail.
     */
    public ContactShare shareContact(Contact contact, User owner, User sharedWith, String permission) {
        log.info("Sharing contact id={} from user id={} to user id={} with permission={}", 
                 contact.getId(), owner.getId(), sharedWith.getId(), permission);

        // Prevent self-sharing (no reason to share a contact with yourself).
        if (owner.getId().equals(sharedWith.getId())) {
            throw new IllegalArgumentException("Cannot share a contact with yourself");
        }

        // Check if already shared; update instead of create.
        if (contactShareRepository.existsByContactIdAndSharedWithId(contact.getId(), sharedWith.getId())) {
            log.warn("Contact already shared; updating permission for contact id={}, user id={}", 
                     contact.getId(), sharedWith.getId());
            return updateSharePermission(contact.getId(), sharedWith.getId(), permission);
        }

        ContactShare share = new ContactShare(contact, owner, sharedWith, permission);
        contactShareRepository.save(share);
        log.info("Contact shared successfully; share id={}", share.getId());
        return share;
    }

    /**
     * Update the permission level for an existing share.
     */
    public ContactShare updateSharePermission(Long contactId, Long sharedWithId, String permission) {
        log.info("Updating share permission for contact id={}, user id={} to {}", 
                 contactId, sharedWithId, permission);

        ContactShare share = contactShareRepository.findByContactIdAndSharedWithId(contactId, sharedWithId)
            .orElseThrow(() -> new ResourceNotFoundException("Share not found"));

        share.setPermission(permission);
        share.setUpdatedAt(LocalDateTime.now());
        contactShareRepository.save(share);
        log.info("Share permission updated; share id={}", share.getId());
        return share;
    }

    /**
     * Revoke access to a shared contact.
     */
    public void revokeShare(Long contactId, Long sharedWithId) {
        log.info("Revoking share for contact id={}, user id={}", contactId, sharedWithId);

        ContactShare share = contactShareRepository.findByContactIdAndSharedWithId(contactId, sharedWithId)
            .orElseThrow(() -> new ResourceNotFoundException("Share not found"));

        contactShareRepository.delete(share);
        log.info("Share revoked; contact id={}, user id={}", contactId, sharedWithId);
    }

    /**
     * Remove every share record for a contact before deleting the contact itself.
     * This keeps batch delete cleanup straightforward and avoids orphaned share rows.
     */
    public void revokeAllSharesForContact(Long contactId) {
        log.info("Revoking all shares for contact id={}", contactId);

        List<ContactShare> shares = contactShareRepository.findByContactId(contactId);
        if (!shares.isEmpty()) {
            contactShareRepository.deleteAll(shares);
        }

        log.info("Revoked {} shares for contact id={}", shares.size(), contactId);
    }

    /**
     * Get all contacts shared with a user.
     */
    public List<ContactShare> getContactsSharedWithUser(Long userId) {
        log.debug("Fetching contacts shared with user id={}", userId);
        return contactShareRepository.findBySharedWithId(userId);
    }

    /**
     * Check if a user has access to a contact.
     * Owner always has access; others only if explicitly shared.
     */
    public boolean hasAccess(Contact contact, User user) {
        // Owner always has full access
        if (contact.getUser().getId().equals(user.getId())) {
            return true;
        }
        // Otherwise, check if contact is shared with this user
        return contactShareRepository.existsByContactIdAndSharedWithId(contact.getId(), user.getId());
    }

    /**
     * Check if a user has write permission to a contact.
     */
    public boolean hasWriteAccess(Contact contact, User user) {
        // Owner always has write access
        if (contact.getUser().getId().equals(user.getId())) {
            return true;
        }
        // Check if shared with "write" permission
        return contactShareRepository.findByContactIdAndSharedWithId(contact.getId(), user.getId())
            .map(share -> "write".equalsIgnoreCase(share.getPermission()))
            .orElse(false);
    }
}
