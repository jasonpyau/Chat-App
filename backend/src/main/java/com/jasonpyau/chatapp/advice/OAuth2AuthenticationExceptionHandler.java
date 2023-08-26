// package com.jasonpyau.chatapp.advice;

// import java.net.URI;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.ControllerAdvice;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.util.UriComponentsBuilder;

// import com.jasonpyau.chatapp.exception.OAuth2AuthenticationException;

// @ControllerAdvice
// public class OAuth2AuthenticationExceptionHandler {
    
//     @ExceptionHandler(OAuth2AuthenticationException.class)
//     public ResponseEntity<Void> handleOAuth2AuthenticationException(OAuth2AuthenticationException e) {
//         System.out.println("error");
//         URI uri = UriComponentsBuilder.fromPath("/login").queryParam("error", e.getMessage()).build().toUri();
//         return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
//     }
// }
