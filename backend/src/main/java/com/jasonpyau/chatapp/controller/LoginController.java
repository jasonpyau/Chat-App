package com.jasonpyau.chatapp.controller;

import java.net.URI;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jasonpyau.chatapp.annotation.GetUser;
import com.jasonpyau.chatapp.annotation.RateLimitAPI;
import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.entity.User.Role;
import com.jasonpyau.chatapp.service.UserService;
import com.jasonpyau.chatapp.service.RateLimitService.Token;
import com.jasonpyau.chatapp.util.Response;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping(path = "/principal", produces = MediaType.APPLICATION_JSON_VALUE)
    public OAuth2User principal(@AuthenticationPrincipal OAuth2User user) {
        return user;
    }
    
    @GetMapping(path = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, Object>> user(@GetUser User user) {
        String[] keys = {"user", "loggedIn", "newUser"};
        Boolean loggedIn = (user != null), newUser = (loggedIn && user.getRole().equals(Role.NEW_USER));
        Object[] vals = {user, loggedIn, newUser};
        return new ResponseEntity<>(Response.createBody(keys, vals), HttpStatus.OK);
    }

    @PostMapping(path = "/new_user", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @RateLimitAPI(Token.DEFAULT_TOKEN)
    public ResponseEntity<Void> newUser(@GetUser User user, @RequestParam("username") String username, @AuthenticationPrincipal OAuth2User oAuth2User) {
        userService.newUser(user, username, oAuth2User);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/")).build();
    }

}
