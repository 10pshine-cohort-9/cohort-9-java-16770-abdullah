package com.abdullahadil.contactmanagement.dto;

import com.abdullahadil.contactmanagement.entity.ContactLabel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContactPhoneRequest(
        @NotNull ContactLabel label,
        @NotBlank String phoneNumber
) {
}
