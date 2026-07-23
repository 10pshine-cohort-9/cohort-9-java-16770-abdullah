package com.abdullahadil.contactmanagement.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Self-registration accepts either an email or a phone number (or both) —
 * the spec requires supporting either identifier, not just email.
 */
public record RegisterRequest(
        @Email String email,
        String phoneNumber,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password
) {

    @AssertTrue(message = "Either an email or a phone number is required")
    public boolean isEmailOrPhonePresent() {
        return (email != null && !email.isBlank()) || (phoneNumber != null && !phoneNumber.isBlank());
    }
}
