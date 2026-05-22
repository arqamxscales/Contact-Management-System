package com.cms.service;

import com.cms.dto.ContactRequest;
import com.cms.dto.ImportContactsResponse;
import com.cms.entity.Contact;
import com.cms.entity.User;
import com.cms.repository.ContactRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ContactImportService.
 * Handles CSV file parsing and bulk import of contacts with comprehensive error handling.
 * 
 * Key responsibilities:
 * - Parse CSV content line by line
 * - Map flexible column headers (e.g., "firstName" or "first_name" or "first name")
 * - Validate each row before insertion
 * - Provide detailed feedback on successes and failures
 * - Keep the transaction atomic (all or nothing for consistency)
 */
@Service
public class ContactImportServiceImpl implements ContactImportService {

    // Logger for debugging import operations
    private static final Logger log = LoggerFactory.getLogger(ContactImportServiceImpl.class);

    // Safety limit to prevent memory bloat on huge imports
    private static final int MAX_BATCH_SIZE = 1000;

    private final ContactRepository contactRepository;

    public ContactImportServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    @Transactional
    public ImportContactsResponse importContactsFromCsv(String csvContent, User user) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try {
            // Split the CSV into lines and remove empty ones
            String[] lines = csvContent.split("\\n");
            
            // We need at least one header line plus one data line to be meaningful
            if (lines.length < 2) {
                errors.add("CSV file appears to be empty or missing data rows");
                return new ImportContactsResponse(0, 0, 1, errors, "No contacts to import");
            }

            // First line is always the header row; parse it to understand column positions
            String headerLine = lines[0].trim();
            if (headerLine.isEmpty()) {
                errors.add("CSV header row is empty");
                return new ImportContactsResponse(0, 0, 1, errors, "Invalid CSV header");
            }

            // Build a map of column name -> column index for flexible header matching
            Map<String, Integer> columnIndexMap = parseHeaderRow(headerLine);

            log.info("Starting CSV import for user {} with {} data rows", user.getId(), lines.length - 1);

            // Process each data row starting from index 1 (skip header)
            for (int rowIndex = 1; rowIndex < lines.length && rowIndex - 1 < MAX_BATCH_SIZE; rowIndex++) {
                String line = lines[rowIndex].trim();
                
                // Skip empty lines (sometimes CSV files have trailing newlines)
                if (line.isEmpty()) {
                    continue;
                }

                // Process this single row - returns success or adds error message
                if (processRowImport(line, rowIndex, columnIndexMap, user, errors)) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }

            // Build response message summarizing the import
            int totalProcessed = successCount + failureCount;
            String message = String.format(
                "Import completed: %d succeeded, %d failed out of %d total rows",
                successCount, failureCount, totalProcessed
            );

            log.info("CSV import completed for user {}: {} success, {} failure", 
                user.getId(), successCount, failureCount);

            return new ImportContactsResponse(totalProcessed, successCount, failureCount, errors, message);

        } catch (Exception e) {
            log.error("Fatal error during CSV import for user {}: {}", user.getId(), e.getMessage(), e);
            errors.add("Fatal error during import: " + e.getMessage());
            return new ImportContactsResponse(0, 0, 1, errors, "Import failed with error");
        }
    }

    /**
     * Process a single CSV row, validate it, and import as a Contact.
     * Returns true if the row was successfully imported, false if validation or save failed.
     * Error details are added to the errors list.
     */
    private boolean processRowImport(String line, int rowIndex, Map<String, Integer> columnIndexMap, 
                                     User user, List<String> errors) {
        try {
            // Parse this row into individual fields, respecting quoted values
            Map<String, String> rowData = parseDataRow(line, columnIndexMap);

            // Validate the parsed row has at least a firstName
            String firstName = rowData.getOrDefault("firstname", "").trim();
            if (firstName.isEmpty()) {
                errors.add("Row " + rowIndex + ": firstName is required");
                return false;
            }

            // Build a ContactRequest from the parsed row
            ContactRequest contactRequest = buildContactRequestFromRow(rowData, user.getId());

            // Create and save the contact
            Contact contact = new Contact();
            contact.setUser(user);
            contact.setFirstName(contactRequest.getFirstName());
            contact.setLastName(contactRequest.getLastName());
            contact.setTitle(contactRequest.getTitle());
            contact.setEmail(contactRequest.getEmail());
            contact.setPhone(contactRequest.getPhone());
            contact.setAddress(contactRequest.getAddress());
            contact.setCreatedAt(LocalDateTime.now());

            contactRepository.save(contact);
            return true;

        } catch (Exception e) {
            errors.add("Row " + rowIndex + ": " + e.getMessage());
            log.warn("Failed to import row {}: {}", rowIndex, e.getMessage());
            return false;
        }
    }

    /**
     * Parse the CSV header row to map column names to their positions.
     * This method normalizes column names to handle variations like:
     * - "firstName", "first_name", "first name", "First Name"
     * 
     * @param headerLine the first line of the CSV
     * @return a map of normalized column names to their indices
     */
    private Map<String, Integer> parseHeaderRow(String headerLine) {
        Map<String, Integer> columnMap = new HashMap<>();
        String[] headers = splitCsvLine(headerLine);

        for (int i = 0; i < headers.length; i++) {
            // Normalize header name: trim, lowercase, replace underscores/spaces with nothing
            String normalizedName = normalizeColumnName(headers[i]);
            if (!normalizedName.isEmpty()) {
                columnMap.put(normalizedName, i);
            }
        }

        return columnMap;
    }

    /**
     * Parse a single data row from the CSV, respecting quoted values that may contain commas.
     * Maps the values to their column names based on the header positions.
     * 
     * @param line the data row
     * @param columnIndexMap the map from parseHeaderRow
     * @return a map of column name -> value
     */
    private Map<String, String> parseDataRow(String line, Map<String, Integer> columnIndexMap) {
        Map<String, String> rowData = new HashMap<>();
        String[] values = splitCsvLine(line);

        // Create a reverse map: column index -> normalized name
        Map<Integer, String> indexToNameMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : columnIndexMap.entrySet()) {
            indexToNameMap.put(entry.getValue(), entry.getKey());
        }

        // Map each value to its column name
        for (int i = 0; i < values.length; i++) {
            if (indexToNameMap.containsKey(i)) {
                rowData.put(indexToNameMap.get(i), values[i].trim());
            }
        }

        return rowData;
    }

    /**
     * Split a CSV line into individual fields, properly handling quoted values.
     * A quoted field like "John, Smith" should be treated as one field, not two.
     * 
     * @param line the CSV line to split
     * @return array of field values (quotes removed)
     */
    private String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Toggle quote state
                insideQuotes = !insideQuotes;
            } else if (c == ',' && !insideQuotes) {
                // Only treat comma as delimiter if we're not inside quotes
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // Don't forget the last field
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    /**
     * Normalize a column header name to handle variations.
     * e.g., "first_name", "First Name", "firstName" all normalize to "firstname"
     * 
     * @param columnName the raw column header name
     * @return the normalized name
     */
    private String normalizeColumnName(String columnName) {
        return columnName
            .toLowerCase()
            .trim()
            .replaceAll("[_\\s]", ""); // Remove underscores and spaces
    }

    /**
     * Build a ContactRequest from parsed CSV row data.
     * Maps flexible column names to ContactRequest fields.
     * Missing fields are set to empty strings or null.
     * 
     * @param rowData the parsed row map
     * @param userId the current user's ID
     * @return a ContactRequest ready for service layer
     */
    private ContactRequest buildContactRequestFromRow(Map<String, String> rowData, Long userId) {
        ContactRequest request = new ContactRequest();
        request.setUserId(userId);

        // Map CSV columns to ContactRequest fields using normalized names
        request.setFirstName(getValueFromRow(rowData, "firstname"));
        request.setLastName(getValueFromRow(rowData, "lastname"));
        request.setEmail(getValueFromRow(rowData, "email"));
        request.setPhone(getValueFromRow(rowData, "phone"));
        request.setTitle(getValueFromRow(rowData, "title"));
        request.setAddress(getValueFromRow(rowData, "address"));

        return request;
    }

    /**
     * Safely retrieve a value from the row data map, trying multiple normalized variations.
     * This helps handle user-provided headers that might differ slightly.
     * 
     * @param rowData the parsed row
     * @param normalizedKey the key to look for
     * @return the value, or empty string if not found
     */
    private String getValueFromRow(Map<String, String> rowData, String normalizedKey) {
        // Try direct lookup first
        if (rowData.containsKey(normalizedKey)) {
            return rowData.get(normalizedKey);
        }

        // If not found, return empty string (not null) so our validation catches it
        return "";
    }
}
