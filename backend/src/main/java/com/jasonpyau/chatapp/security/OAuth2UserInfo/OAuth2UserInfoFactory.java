package com.jasonpyau.chatapp.security.OAuth2UserInfo;

import java.util.Map;

import com.jasonpyau.chatapp.entity.User.AuthenticationProvider;
import com.jasonpyau.chatapp.exception.OAuth2AuthenticationProcessingException;

public class OAuth2UserInfoFactory {
    
    private OAuth2UserInfoFactory() {}

    public static OAuth2UserInfo get(AuthenticationProvider provider, Map<String, Object> attributes) {
        switch (provider) {
            case GOOGLE:
                return new GoogleOAuth2UserInfo(attributes);
            case GITHUB:
                return new GitHubOAuth2UserInfo(attributes);
            default:
                throw new OAuth2AuthenticationProcessingException("OAuth2 Provider not found");
        }
    }
}
