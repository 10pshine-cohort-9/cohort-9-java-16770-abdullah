package com.abdullahadil.contactmanagement.dto;

import java.time.Instant;

public record UserResponse(Long id, String email, String phoneNumber, Instant createdAt) {
}
