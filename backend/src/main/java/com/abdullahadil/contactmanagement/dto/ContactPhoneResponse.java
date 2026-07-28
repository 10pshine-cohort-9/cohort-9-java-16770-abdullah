package com.abdullahadil.contactmanagement.dto;

import com.abdullahadil.contactmanagement.entity.ContactLabel;

public record ContactPhoneResponse(Long id, ContactLabel label, String phoneNumber) {
}
