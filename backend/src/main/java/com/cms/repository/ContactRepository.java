package com.cms.repository;

import com.cms.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for Contact entity.
 * Provides database operations for contact management.
 */
public interface ContactRepository extends JpaRepository<Contact, Long> {

    /**
     * Search contacts by first or last name with pagination support.
     */
    Page<Contact> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName,
        String lastName,
        Pageable pageable
    );
}