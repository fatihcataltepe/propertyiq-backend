package com.propertyiq.portfolio.exception;

public class PropertyAccessDeniedException extends RuntimeException {
    public PropertyAccessDeniedException(String propertyId, String userId) {
        super("User " + userId + " does not have access to property " + propertyId);
    }
}
