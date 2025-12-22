package com.propertyiq.portfolio.exception;

import com.propertyiq.common.exception.ResourceNotFoundException;

public class PropertyNotFoundException extends ResourceNotFoundException {
    public PropertyNotFoundException(String propertyId) {
        super("Property not found with id: " + propertyId);
    }
}
