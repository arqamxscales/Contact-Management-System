package com.cms.repository;

import com.cms.entity.Contact;
import com.cms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for Contact entity with comprehensive search and filtering.
 * Provides database operations for contact management including:
 * - Name-based search (first/last name)
 * - Email and phone search with formatting flexibility
 * - Label-based filtering (email label, phone label)
 * - Pagination support for all search operations
 */
public interface ContactRepository extends JpaRepository<Contact, Long> {

    /**
     * Search contacts by first or last name with pagination support.
     * Case-insensitive partial matching for user-friendly search.
     */
    Page<Contact> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName,
        String lastName,
        Pageable pageable
    );

    /**
     * Find all contacts for a specific user with pagination.
     * User-scoped query ensures data isolation between users.
     */
    Page<Contact> findByUser(User user, Pageable pageable);

    /**
     * Search contacts by name (first or last) within user's contacts.
     * Case-insensitive partial match - finds names containing search term.
     */
    @Query("SELECT c FROM Contact c WHERE c.user = :user AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Contact> searchByName(@Param("user") User user, 
                               @Param("searchTerm") String searchTerm, 
                               Pageable pageable);

    /**
     * Search contacts by email address across all emails.
     * Case-insensitive matching - finds email addresses containing search term.
     */
    @Query("SELECT DISTINCT c FROM Contact c " +
           "LEFT JOIN c.emails e WHERE c.user = :user AND " +
           "LOWER(e.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Contact> searchByEmail(@Param("user") User user, 
                                @Param("searchTerm") String searchTerm, 
                                Pageable pageable);

    /**
     * Search contacts by phone number across all phones.
     * Removes formatting characters (-, parentheses) for flexible matching.
     */
    @Query("SELECT DISTINCT c FROM Contact c " +
           "LEFT JOIN c.phones p WHERE c.user = :user AND " +
           "REPLACE(REPLACE(p.number, '-', ''), '(', '') LIKE " +
           "CONCAT('%', :searchTerm, '%')")
    Page<Contact> searchByPhone(@Param("user") User user, 
                               @Param("searchTerm") String searchTerm, 
                               Pageable pageable);

    /**
     * Filter contacts by email label (work, personal, other).
     * Returns contacts that have at least one email with the specified label.
     */
    @Query("SELECT DISTINCT c FROM Contact c " +
           "LEFT JOIN c.emails e WHERE c.user = :user AND e.label = :label")
    Page<Contact> filterByEmailLabel(@Param("user") User user, 
                                     @Param("label") String label, 
                                     Pageable pageable);

    /**
     * Filter contacts by phone label (work, home, mobile, other).
     * Returns contacts that have at least one phone with the specified label.
     */
    @Query("SELECT DISTINCT c FROM Contact c " +
           "LEFT JOIN c.phones p WHERE c.user = :user AND p.label = :label")
    Page<Contact> filterByPhoneLabel(@Param("user") User user, 
                                    @Param("label") String label, 
                                    Pageable pageable);

    /**
     * Combined search and filter: search term + email label.
     * Finds contacts matching search term that also have email with specified label.
     */
    @Query("SELECT DISTINCT c FROM Contact c " +
           "LEFT JOIN c.emails e WHERE c.user = :user AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(e.label = :emailLabel OR :emailLabel IS NULL)")
    Page<Contact> searchWithEmailFilter(@Param("user") User user,
                                       @Param("searchTerm") String searchTerm,
                                       @Param("emailLabel") String emailLabel,
                                       Pageable pageable);

    /**
     * Combined search and filter: search term + phone label.
     * Finds contacts matching search term that also have phone with specified label.
     */
    @Query("SELECT DISTINCT c FROM Contact c " +
           "LEFT JOIN c.phones p WHERE c.user = :user AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "REPLACE(REPLACE(p.number, '-', ''), '(', '') LIKE CONCAT('%', :searchTerm, '%')) AND " +
           "(p.label = :phoneLabel OR :phoneLabel IS NULL)")
    Page<Contact> searchWithPhoneFilter(@Param("user") User user,
                                       @Param("searchTerm") String searchTerm,
                                       @Param("phoneLabel") String phoneLabel,
                                       Pageable pageable);

    /**
     * Checks whether a user already owns a contact with the given email.
     * Prevents duplicate email addresses within user's contact list.
     */
    boolean existsByUserIdAndEmailIgnoreCase(Long userId, String email);

    /**
     * Same duplicate-email check but excludes current contact id for updates.
     * Allows updating contact without triggering false duplicate error.
     */
    boolean existsByUserIdAndEmailIgnoreCaseAndIdNot(Long userId, String email, Long id);
}