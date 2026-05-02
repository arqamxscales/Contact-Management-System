package com.cms.repository;

import com.cms.entity.ContactShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ContactShare entity.
 * Manages contact sharing and access control queries.
 */
@Repository
public interface ContactShareRepository extends JpaRepository<ContactShare, Long> {
    // Find all shares for a specific contact
    List<ContactShare> findByContactId(Long contactId);
    
    // Find all contacts shared with a specific user
    List<ContactShare> findBySharedWithId(Long userId);
    
    // Find all contacts owned by a user
    List<ContactShare> findByOwnerId(Long userId);
    
    // Check if a contact is shared with a specific user
    boolean existsByContactIdAndSharedWithId(Long contactId, Long userId);
    
    // Find a specific share record
    Optional<ContactShare> findByContactIdAndSharedWithId(Long contactId, Long userId);
}
