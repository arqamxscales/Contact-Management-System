package com.cms.dto;

/**
 * DTO for batch operation response.
 * Returns summary of successful and failed operations for delete/export actions.
 * Used to inform user about batch operation results.
 */
public class BatchOperationResponse {

    // Total number of items requested for operation
    private Integer totalRequested;

    // Number of items successfully processed
    private Integer successCount;

    // Number of items that failed
    private Integer failureCount;

    // Operation type (delete, export, etc.)
    private String operationType;

    // Success message to display to user
    private String message;

    public BatchOperationResponse() {
    }

    public BatchOperationResponse(Integer totalRequested, Integer successCount, 
                                 Integer failureCount, String operationType, String message) {
        this.totalRequested = totalRequested;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.operationType = operationType;
        this.message = message;
    }

    public Integer getTotalRequested() {
        return totalRequested;
    }

    public void setTotalRequested(Integer totalRequested) {
        this.totalRequested = totalRequested;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Utility: check if all operations were successful
    public boolean isAllSuccessful() {
        return failureCount == 0;
    }

    // Utility: check if any operations succeeded
    public boolean hasAnySuccess() {
        return successCount > 0;
    }
}
