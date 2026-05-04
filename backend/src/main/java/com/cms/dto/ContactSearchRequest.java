package com.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

/**
 * DTO for contact search and filtering requests.
 * Supports searching by name, email, phone and filtering by label.
 * Implements pagination via page/size parameters.
 */
public class ContactSearchRequest {

    // Search keyword for name, email, or phone matching
    private String searchTerm;

    // Filter by email label (work, personal, other)
    private String emailLabel;

    // Filter by phone label (work, home, mobile, other)
    private String phoneLabel;

    // Pagination: which page to return (0-indexed)
    @Min(value = 0, message = "Page number must be >= 0")
    private Integer page = 0;

    // Pagination: number of results per page
    @Min(value = 1, message = "Page size must be > 0")
    private Integer size = 20;

    // Sort field (name, email, phone, createdAt)
    private String sortBy = "createdAt";

    // Sort direction (asc, desc)
    private String sortDirection = "desc";

    public ContactSearchRequest() {
    }

    public ContactSearchRequest(String searchTerm, Integer page, Integer size) {
        this.searchTerm = searchTerm;
        this.page = page;
        this.size = size;
    }

    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getEmailLabel() {
        return emailLabel;
    }

    public void setEmailLabel(String emailLabel) {
        this.emailLabel = emailLabel;
    }

    public String getPhoneLabel() {
        return phoneLabel;
    }

    public void setPhoneLabel(String phoneLabel) {
        this.phoneLabel = phoneLabel;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page != null ? page : 0;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size != null && size > 0 ? size : 20;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy != null ? sortBy : "createdAt";
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection != null ? sortDirection : "desc";
    }

    // Utility method: check if search is actually filtering (not just empty)
    public boolean hasSearchCriteria() {
        return (searchTerm != null && !searchTerm.trim().isEmpty()) ||
               (emailLabel != null && !emailLabel.trim().isEmpty()) ||
               (phoneLabel != null && !phoneLabel.trim().isEmpty());
    }
}
