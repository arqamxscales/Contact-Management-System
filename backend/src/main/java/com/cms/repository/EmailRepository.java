package com.cms.repository;

import com.cms.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Email entity.
 * Handles queries for email addresses associated with contacts.
 */
@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
    // Find all emails for a specific contact
    List<Email> findByContactId(Long contactId);
    
    // Find all emails by label (e.g., all "work" emails across all contacts of a user)
    List<Email> findByLabel(String label);
    
    // Find primary email for a contact (if marked as primary)
    Email findByContactIdAndIsPrimaryTrue(Long contactId);
    
    // Check if an email address already exists for a user's contacts (prevent duplicates)
    boolean existsByContactUserIdAndAddress(Long userId, String address);
}
