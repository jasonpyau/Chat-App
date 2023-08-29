package com.jasonpyau.chatapp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jasonpyau.chatapp.service.RateLimitService.Token;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimitAPI {

    public Token value() default Token.DEFAULT_TOKEN;
}
