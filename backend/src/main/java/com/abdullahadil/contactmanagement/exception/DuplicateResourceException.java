package com.abdullahadil.contactmanagement.exception;

/**
 * Thrown when a create/register operation would violate a uniqueness
 * constraint (e.g. registering with an email or phone number that is
 * already in use). Translated to a 409 by GlobalExceptionHandler.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
