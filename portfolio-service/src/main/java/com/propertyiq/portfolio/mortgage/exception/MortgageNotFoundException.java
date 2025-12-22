package com.propertyiq.portfolio.mortgage.exception;

import com.propertyiq.common.exception.ResourceNotFoundException;

public class MortgageNotFoundException extends ResourceNotFoundException {
    public MortgageNotFoundException(String mortgageId) {
        super("Mortgage not found with id: " + mortgageId);
    }
}
