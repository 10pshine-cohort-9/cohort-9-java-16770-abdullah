package com.abdullahadil.contactmanagement.dto;

import jakarta.validation.constraints.NotBlank;

/** Login accepts either an email or a phone number as the identifier. */
public record LoginRequest(
        @NotBlank String identifier,
        @NotBlank String password
) {
}
