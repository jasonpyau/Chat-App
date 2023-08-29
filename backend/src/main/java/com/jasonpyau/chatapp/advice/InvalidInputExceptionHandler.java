package com.jasonpyau.chatapp.advice;

import java.net.URI;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.jasonpyau.chatapp.exception.InvalidGroupChatException;
import com.jasonpyau.chatapp.exception.InvalidInputException;
import com.jasonpyau.chatapp.exception.InvalidUsernameException;
import com.jasonpyau.chatapp.util.Response;

@ControllerAdvice
public class InvalidInputExceptionHandler {
    
    @ExceptionHandler(InvalidUsernameException.class)
    public ResponseEntity<Void> handleInvalidUsernameException(InvalidUsernameException e) {
        URI uri = UriComponentsBuilder.fromPath("/new_user")
        .queryParam("error", e.getMessage())
        .queryParam("username", e.getUsername())
        .build().toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
    }

    @ExceptionHandler(InvalidGroupChatException.class)
    public ResponseEntity<HashMap<String, Object>> InvalidGroupChatException(InvalidGroupChatException e) {
        return Response.errorMessage(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<HashMap<String, Object>> handleInvalidInputException(InvalidInputException e) {
        return Response.errorMessage(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
    }

    @MessageExceptionHandler(InvalidInputException.class)
    @SendToUser("/topic/errors")
    public String handleInvalidInputWebSocketException(InvalidInputException e) {
        return "Invalid input.";
    }
}
