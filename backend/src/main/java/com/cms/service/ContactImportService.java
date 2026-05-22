package com.cms.service;

import com.cms.dto.ImportContactsResponse;
import com.cms.entity.User;

/**
 * Service interface for handling bulk contact imports from CSV files.
 * Manages parsing, validation, and batch insertion of contacts.
 */
public interface ContactImportService {

    /**
     * Import contacts from a CSV file content (as string).
     * The CSV is expected to have headers like firstName, lastName, email, phone, etc.
     * 
     * @param csvContent the raw CSV file content (already read from multipart upload)
     * @param user the authenticated user who owns these imported contacts
     * @return ImportContactsResponse with success count, failure count, and error messages
     */
    ImportContactsResponse importContactsFromCsv(String csvContent, User user);
}
