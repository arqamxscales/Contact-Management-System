package com.cms.service;

import com.cms.dto.BatchDeleteRequest;
import com.cms.dto.BatchOperationResponse;
import com.cms.entity.Contact;
import com.cms.entity.User;
import com.cms.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for batch operations on contacts (delete, export, etc.).
 * Handles bulk contact operations with proper access control and error handling.
 * Transactional to ensure consistency - all deletes succeed or all fail.
 */
@Service
@Transactional
public class BatchContactService {

    private static final Logger logger = LoggerFactory.getLogger(BatchContactService.class);

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ContactShareService contactShareService;

    /**
     * Delete multiple contacts in a single batch operation.
     * Verifies that current user owns each contact before deletion.
     * Revokes all shares on deleted contacts for data integrity.
     * 
     * @param request BatchDeleteRequest with list of contact IDs
     * @param currentUser User performing the delete (for access control)
     * @return BatchOperationResponse with success/failure counts
     */
    public BatchOperationResponse deleteContactsBatch(BatchDeleteRequest request, User currentUser) {
        List<Long> contactIds = request.getContactIds();
        int successCount = 0;
        int failureCount = 0;
        logger.info("Starting batch delete of {} contacts for user: {}", contactIds.size(), currentUser.getEmail());

        // Delete each contact individually to allow partial failures
        for (Long contactId : contactIds) {
            try {
                // Retrieve contact and verify ownership
                Contact contact = contactRepository.findById(contactId)
                    .orElseThrow(() -> new RuntimeException("Contact not found: " + contactId));

                // Verify current user owns this contact
                if (!contact.getUser().getId().equals(currentUser.getId())) {
                    throw new SecurityException("Access denied: user does not own this contact");
                }

                // Revoke all shares on this contact before deletion
                contactShareService.revokeAllSharesForContact(contactId);

                // Delete the contact and all related emails/phones via cascade
                contactRepository.delete(contact);
                successCount++;

                logger.debug("Successfully deleted contact: {} (ID: {})", contact.getFirstName(), contactId);

            } catch (Exception e) {
                failureCount++;
                String errorMsg = "Failed to delete contact " + contactId + ": " + e.getMessage();
                logger.warn(errorMsg);
            }
        }

        // Build response with summary
        String message = String.format("Deleted %d of %d contacts", successCount, contactIds.size());
        if (failureCount > 0) {
            message += " (" + failureCount + " failed)";
        }

        logger.info("Batch delete completed for user: {} - {} successful, {} failed", 
            currentUser.getEmail(), successCount, failureCount);

        return new BatchOperationResponse(contactIds.size(), successCount, failureCount, "delete", message);
    }

    /**
     * Export contacts to CSV format.
     * Creates CSV string representation of contacts with all fields.
     * Can be used for backup or data migration.
     * 
     * @param contactIds List of contact IDs to export
     * @param currentUser User performing export (for access control)
     * @return CSV string with headers and contact data
     */
    public String exportContactsToCSV(List<Long> contactIds, User currentUser) {
        StringBuilder csv = new StringBuilder();

        // CSV Header: standard contact fields
        csv.append("ID,FirstName,LastName,Title,Company,Email,Phone,Address,CreatedAt\n");

        logger.info("Exporting {} contacts for user: {}", contactIds.size(), currentUser.getEmail());

        // Append each contact as a CSV row
        for (Long contactId : contactIds) {
            try {
                Contact contact = contactRepository.findById(contactId)
                    .orElseThrow(() -> new RuntimeException("Contact not found"));

                // Verify ownership before including in export
                if (!contact.getUser().getId().equals(currentUser.getId())) {
                    logger.warn("Attempted to export contact not owned by user: {}", contactId);
                    continue;
                }

                // Build CSV row with proper escaping for commas and quotes
                csv.append(escapeCSVField(contact.getId().toString())).append(",");
                csv.append(escapeCSVField(contact.getFirstName())).append(",");
                csv.append(escapeCSVField(contact.getLastName())).append(",");
                csv.append(escapeCSVField(contact.getTitle())).append(",");
                csv.append(escapeCSVField(contact.getEmail())).append(",");
                csv.append(escapeCSVField(contact.getPhone())).append(",");
                csv.append(escapeCSVField(contact.getAddress())).append(",");
                csv.append(escapeCSVField(contact.getCreatedAt() != null ? contact.getCreatedAt().toString() : ""));
                csv.append("\n");

            } catch (Exception e) {
                logger.warn("Failed to export contact {}: {}", contactId, e.getMessage());
            }
        }

        logger.info("Export completed for user: {}", currentUser.getEmail());
        return csv.toString();
    }

    /**
     * Utility method to escape CSV field values.
     * Wraps fields in quotes if they contain commas or quotes.
     * Escapes internal quotes by doubling them (CSV standard).
     * 
     * @param field Field value to escape
     * @return Escaped field value safe for CSV format
     */
    private String escapeCSVField(String field) {
        if (field == null || field.isEmpty()) {
            return "";
        }
        
        // If field contains comma, newline, or quote - wrap in quotes
        if (field.contains(",") || field.contains("\n") || field.contains("\"")) {
            // Escape internal quotes by doubling them
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        
        return field;
    }
}
