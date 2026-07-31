package com.abdullahadil.contactmanagement.exception;

/**
 * Thrown when a login identifier (email/phone) doesn't match a user, or
 * the password doesn't match. Deliberately doesn't distinguish which, so
 * the API never reveals whether a given identifier is registered.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
