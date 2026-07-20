package com.abdullahadil.contactmanagement.exception;

/**
 * Thrown when a requested entity (e.g. a contact or user) cannot be
 * found by its identifier. Translated to a 404 by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
