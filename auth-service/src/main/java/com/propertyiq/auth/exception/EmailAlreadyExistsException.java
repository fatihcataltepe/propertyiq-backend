package com.propertyiq.auth.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    
    public EmailAlreadyExistsException(String email) {
        super("User with email '" + email + "' already exists");
    }
}
