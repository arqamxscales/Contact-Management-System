package com.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for a labeled email address.
 * Allows clients to send/receive email data with a label (work, personal, etc.)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabeledEmailDto {

    @NotBlank(message = "Email label is required")
    private String label; // e.g., "work", "personal"

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be valid")
    private String address;

    private Boolean isPrimary = false;
}
