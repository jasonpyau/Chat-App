package com.jasonpyau.chatapp.service;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.entity.User.Role;
import com.jasonpyau.chatapp.exception.InvalidInputException;
import com.jasonpyau.chatapp.exception.InvalidUsernameException;
import com.jasonpyau.chatapp.repository.UserRepository;
import com.jasonpyau.chatapp.security.CustomOAuth2User;
import com.jasonpyau.chatapp.util.CustomValidator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private final CustomValidator<User> customValidator = new CustomValidator<>();

    public User currentUser(CustomOAuth2User oAuth2User) {
        return oAuth2User.getUser();
    }

    public void newUser(User user, String username, OAuth2User oAuth2User) {
        if (userRepository.existsByUsername(username)) {
            throw new InvalidUsernameException(username+" is already taken. Try a different username.", username);
        }
        user.setUsername(username);
        user.setRole(Role.USER);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            throw new InvalidUsernameException(new ConstraintViolationException(violations).getMessage(), username);
        }
        // Make sure the OAuth2User has updated authorities, as it's not refreshed until user re-logins.
        CustomOAuth2User customOAuth2User = (CustomOAuth2User)oAuth2User;
        customOAuth2User.refreshAuthority();
        // Update for the Security Config.
        Authentication newAuth = new OAuth2AuthenticationToken(customOAuth2User, customOAuth2User.getAuthorities(), customOAuth2User.getName());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        userRepository.save(user);
    }

    public User findByUsername(String username) {
        Optional<User> optional = userRepository.findByUsername(username);
        if (optional.isEmpty()) {
            return null;
        }
        return optional.get();
    }

    public User findUserJoinedWithGroupChat(String username) {
        Optional<User> optional = userRepository.findUserJoinedWithGroupChat(username);
        if (optional.isEmpty()) {
            throw new InvalidInputException(username+" is invalid.");
        }
        return optional.get();
    }

    public User updateDisplayName(User user, String displayName) {
        user.setDisplayName(displayName);
        customValidator.validate(user);
        return userRepository.save(user);
    }

    public User getUserFromWebSocket(Principal principal) {
        CustomOAuth2User oAuth2User = (CustomOAuth2User)((OAuth2AuthenticationToken)principal).getPrincipal();
        return oAuth2User.getUser();
    }

}
