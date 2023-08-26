package com.jasonpyau.chatapp.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.jasonpyau.chatapp.entity.User;

import lombok.Getter;

public class CustomOAuth2User implements OAuth2User {
    
    @Getter
    private User user;
    private Map<String, Object> attributes;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+user.getRole().toString()));
        this.user = user;
        this.attributes = attributes;     
    }

    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    } 
}
