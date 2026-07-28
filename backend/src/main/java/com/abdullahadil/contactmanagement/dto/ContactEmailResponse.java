package com.abdullahadil.contactmanagement.dto;

import com.abdullahadil.contactmanagement.entity.ContactLabel;

public record ContactEmailResponse(Long id, ContactLabel label, String email) {
}
