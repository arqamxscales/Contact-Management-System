package com.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for a labeled phone number.
 * Allows clients to send/receive phone data with a label (work, home, mobile, etc.)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabeledPhoneDto {

    @NotBlank(message = "Phone label is required")
    private String label; // e.g., "work", "home", "mobile"

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Phone number must be valid (7-15 digits, optional + prefix)")
    private String number;

    private Boolean isPrimary = false;
}
