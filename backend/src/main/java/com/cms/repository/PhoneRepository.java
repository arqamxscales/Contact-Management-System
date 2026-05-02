package com.cms.repository;

import com.cms.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Phone entity.
 * Handles queries for phone numbers associated with contacts.
 */
@Repository
public interface PhoneRepository extends JpaRepository<Phone, Long> {
    // Find all phones for a specific contact
    List<Phone> findByContactId(Long contactId);
    
    // Find all phones by label (e.g., all "work" phones across all contacts of a user)
    List<Phone> findByLabel(String label);
    
    // Find primary phone for a contact (if marked as primary)
    Phone findByContactIdAndIsPrimaryTrue(Long contactId);
    
    // Check if a phone number already exists for a user's contacts (prevent duplicates)
    boolean existsByContactUserIdAndNumber(Long userId, String number);
}
