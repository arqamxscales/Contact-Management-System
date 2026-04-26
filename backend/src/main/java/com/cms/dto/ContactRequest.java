package com.cms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for contact create/update operations.
 * Keeping validation close to the DTO helps controllers stay clean.
 */
public class ContactRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    // Added today: format validation saves us from junk values in search/detail views.
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String email;

    @Size(max = 25, message = "Phone must be at most 25 characters")
    private String phone;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}