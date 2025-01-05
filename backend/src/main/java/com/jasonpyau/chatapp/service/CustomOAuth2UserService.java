package com.jasonpyau.chatapp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.entity.User.AuthenticationProvider;
import com.jasonpyau.chatapp.entity.User.Role;
import com.jasonpyau.chatapp.exception.OAuth2AuthenticationProcessingException;
import com.jasonpyau.chatapp.repository.UserRepository;
import com.jasonpyau.chatapp.security.CustomOAuth2User;
import com.jasonpyau.chatapp.security.OAuth2UserInfo.OAuth2UserInfo;
import com.jasonpyau.chatapp.security.OAuth2UserInfo.OAuth2UserInfoFactory;
import com.jasonpyau.chatapp.util.DateFormat;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationProcessingException {
        OAuth2User oAuth2User = super.loadUser(request);
        return processOAuth2User(request, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest request, OAuth2User oAuth2User) {
        AuthenticationProvider currentProvider = AuthenticationProvider.valueOf(request.getClientRegistration().getRegistrationId().toUpperCase());
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.get(currentProvider, oAuth2User.getAttributes());
        String email = userInfo.getEmail();
        if (email == null || email.isBlank()) {
            if (currentProvider == AuthenticationProvider.GITHUB) {
                throw new OAuth2AuthenticationProcessingException("No email found from OAuth2 provider. Make sure your email is set to public on your GitHub settings.");
            } else {
                throw new OAuth2AuthenticationProcessingException("No email found from OAuth2 provider.");
            }
        }
        Optional<User> optional = userRepository.findByEmail(email);
        User user;
        if (optional.isPresent()) {
            user = optional.get();
            AuthenticationProvider userProvider = user.getAuthenticationProvider();
            if (userProvider == currentProvider) {
                user = updateExistingUser(user, userInfo);
            } else {
                throw new OAuth2AuthenticationProcessingException("You are signed up with "+userProvider+" with the email "+email+". Please use "+userProvider+" to log in.");
            }
        } else {
            user = registerNewUser(userInfo, currentProvider);
        }
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserInfo userInfo, AuthenticationProvider provider) {
        User user = User.builder()
                        .authenticationProvider(provider)
                        .email(userInfo.getEmail())
                        .displayName((userInfo.getName() != null) ? userInfo.getName() : "User")
                        .avatarURL(userInfo.getAvatarURL())
                        .role(Role.NEW_USER)
                        .createdAt(DateFormat.getUnixTime())
                        .build();
        return userRepository.save(user);
    }

    private User updateExistingUser(User user, OAuth2UserInfo userInfo) {
        user.setAvatarURL(userInfo.getAvatarURL());
        return userRepository.save(user);
    }
}
