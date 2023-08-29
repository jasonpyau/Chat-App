package com.jasonpyau.chatapp.advice;

import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.jasonpyau.chatapp.exception.RateLimitException;
import com.jasonpyau.chatapp.util.Response;

@ControllerAdvice
public class RateLimitExceptionHandler {
    
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<HashMap<String, Object>> handleRateLimitException(RateLimitException e) {
        return Response.rateLimit(e.getMs());
    }
    
    @MessageExceptionHandler(RateLimitException.class)
    @SendToUser("/topic/errors")
    public String handleRateLimitWebSocketException(RateLimitException e) {
        return "Rate limit, try again in "+e.getMs()+"ms.";
    }
}
