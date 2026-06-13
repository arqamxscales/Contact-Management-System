# Use Cases: Contact Import

This file lists the primary use cases covered by the CSV import mockup.

- **UC-001: Import contacts (happy path)**
  - Actor: Authenticated user
  - Precondition: User has a CSV file with at least one data row and correct headers.
  - Steps:
    1. User opens Contacts page and selects "Import".
    2. User uploads a CSV file and submits.
    3. Server parses CSV, validates rows, saves valid contacts.
    4. Server returns `ImportContactsResponse` with success/failure counts and messages.
  - Postcondition: Valid contacts persisted; UI shows summary and any row-level errors.

- **UC-002: Import with partial failures**
  - Actor: Authenticated user
  - Precondition: CSV contains some invalid rows (e.g., missing `firstName`).
  - Steps: Same as UC-001; server records per-row errors and still imports valid rows.
  - Postcondition: Valid rows saved; response contains list of error messages referencing failed rows.

- **UC-003: Empty or header-only CSV**
  - Actor: Authenticated user
  - Precondition: CSV has no data rows.
  - Steps: Server detects missing data rows and returns an error response pointing out the issue.
  - Postcondition: No contacts created; response indicates "No contacts to import".

- **UC-004: File upload/read error**
  - Actor: Authenticated user
  - Precondition: File cannot be read (corrupt or IO error).
  - Steps: Controller catches the exception while reading bytes and responds with a `400 Bad Request` and explanation.
  - Postcondition: No contacts created; response explains file read failure.

Testing notes
- Unit tests cover parsing, quoted-fields, flexible headers, missing-required-fields, skipping empty lines, and large-file behavior (within `MAX_BATCH_SIZE`).
