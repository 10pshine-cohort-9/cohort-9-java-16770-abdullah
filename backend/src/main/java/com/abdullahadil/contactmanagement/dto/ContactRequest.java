package com.abdullahadil.contactmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ContactRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String title,
        @Valid List<ContactEmailRequest> emails,
        @Valid List<ContactPhoneRequest> phones
) {
}
