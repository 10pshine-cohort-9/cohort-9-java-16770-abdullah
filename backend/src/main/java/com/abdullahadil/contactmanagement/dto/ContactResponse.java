package com.abdullahadil.contactmanagement.dto;

import java.time.Instant;
import java.util.List;

public record ContactResponse(
        Long id,
        String firstName,
        String lastName,
        String title,
        List<ContactEmailResponse> emails,
        List<ContactPhoneResponse> phones,
        Instant createdAt,
        Instant updatedAt
) {
}
