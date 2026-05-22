package com.cms.dto;

import java.util.List;

/**
 * Response DTO for bulk contact import operations.
 * Contains summary statistics about the import: how many succeeded, failed, etc.
 * This helps the frontend display meaningful feedback to the user.
 */
public class ImportContactsResponse {

    // Total number of contacts we attempted to import from the CSV
    private int totalProcessed;

    // How many contacts were successfully imported
    private int successCount;

    // How many contacts failed due to validation or other errors
    private int failureCount;

    // List of detailed error messages for each failed row
    private List<String> errors;

    // Descriptive message summarizing the import operation
    private String message;

    public ImportContactsResponse() {
    }

    public ImportContactsResponse(int totalProcessed, int successCount, int failureCount, List<String> errors, String message) {
        this.totalProcessed = totalProcessed;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.errors = errors;
        this.message = message;
    }

    public int getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
