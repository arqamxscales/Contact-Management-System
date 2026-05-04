package com.cms.dto;

import java.util.List;

/**
 * DTO for batch contact delete operation.
 * Client sends list of contact IDs to delete multiple contacts at once.
 * Used for bulk operations in the UI (select multiple + delete button).
 */
public class BatchDeleteRequest {

    // List of contact IDs to delete
    private List<Long> contactIds;

    // Whether to require confirmation (for safety) - caller should have already confirmed
    private Boolean requireConfirmation = false;

    public BatchDeleteRequest() {
    }

    public BatchDeleteRequest(List<Long> contactIds) {
        this.contactIds = contactIds;
    }

    public List<Long> getContactIds() {
        return contactIds;
    }

    public void setContactIds(List<Long> contactIds) {
        this.contactIds = contactIds;
    }

    public Boolean getRequireConfirmation() {
        return requireConfirmation;
    }

    public void setRequireConfirmation(Boolean requireConfirmation) {
        this.requireConfirmation = requireConfirmation;
    }

    // Utility method to check if request is valid
    public boolean isValid() {
        return contactIds != null && !contactIds.isEmpty();
    }

    // Get count of contacts to delete
    public int getCount() {
        return contactIds != null ? contactIds.size() : 0;
    }
}
