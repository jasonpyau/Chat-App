package com.jasonpyau.chatapp.util;

import java.util.Set;

import com.jasonpyau.chatapp.exception.InvalidInputException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class CustomValidator<T> {
    
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    
    public void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new InvalidInputException(new ConstraintViolationException(violations).getMessage());
        }
    }
}
