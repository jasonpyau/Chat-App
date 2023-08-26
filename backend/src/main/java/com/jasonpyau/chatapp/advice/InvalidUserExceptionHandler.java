package com.jasonpyau.chatapp.advice;

import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.jasonpyau.chatapp.exception.InvalidUserException;
import com.jasonpyau.chatapp.util.Response;

@ControllerAdvice
public class InvalidUserExceptionHandler {
    
    @ExceptionHandler(InvalidUserException.class)
    public ResponseEntity<HashMap<String, Object>> handleInvalidUserException(InvalidUserException e) {
        return Response.unauthorized();
    }
}
