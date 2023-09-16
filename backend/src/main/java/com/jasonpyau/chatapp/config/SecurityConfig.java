package com.jasonpyau.chatapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.jasonpyau.chatapp.entity.User.Role;
import com.jasonpyau.chatapp.security.CustomOAuth2AuthenticationFailureHandler;
import com.jasonpyau.chatapp.security.CustomOAuth2AuthenticationSuccessHandler;
import com.jasonpyau.chatapp.security.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler;

    @Autowired
    private CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {
                cors
                    .disable();
            })
            .csrf(crsf -> {
                crsf
                    .disable();
            })
            .authorizeHttpRequests(auth -> {
                auth
                    .requestMatchers("/new_user", "/api/login/new_user").hasRole(Role.NEW_USER.toString())
                    .requestMatchers("/api/groupchat/**", "/api/message/**", "/api/users/**").hasAnyRole(Role.USER.toString(), Role.ADMIN.toString())
                    .requestMatchers("/topic/**", "/app/**", "/ws/**").hasAnyRole(Role.USER.toString(), Role.ADMIN.toString())
                    .requestMatchers("/built/**").permitAll()
                    .requestMatchers("/", "/error", "login", "logout", "/api/login/user", "/api/login/principal").permitAll()
                    .anyRequest().authenticated();
            })
            .oauth2Login(oauth -> {
                oauth
                    .loginPage("/login")
                    .userInfoEndpoint(userInfo -> {
                        userInfo.userService(customOAuth2UserService);
                    })
                    .successHandler(customOAuth2AuthenticationSuccessHandler)
                    .failureHandler(customOAuth2AuthenticationFailureHandler);

            })
            .logout(logout -> {
                logout
                    .logoutSuccessUrl("/")
                    .logoutUrl("/logout")
                    .invalidateHttpSession(true);
            });
        return http.build();
    }

}
