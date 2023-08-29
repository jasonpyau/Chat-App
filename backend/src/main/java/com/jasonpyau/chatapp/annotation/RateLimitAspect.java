package com.jasonpyau.chatapp.annotation;

import java.security.Principal;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jasonpyau.chatapp.entity.User;
import com.jasonpyau.chatapp.exception.RateLimitException;
import com.jasonpyau.chatapp.service.RateLimitService;
import com.jasonpyau.chatapp.service.UserService;

import io.github.bucket4j.ConsumptionProbe;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private UserService userService;

    @Around("@annotation(RateLimitAPI)")
    public Object rateLimitAPI(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        RateLimitAPI rateLimitAnnotation = methodSignature.getMethod().getAnnotation(RateLimitAPI.class);
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof User) {
                User user = (User)arg;
                ConsumptionProbe consumptionProbe = RateLimitService.RateLimiter.rateLimit(user, rateLimitAnnotation.value());
                if (!consumptionProbe.isConsumed()) {
                    throw new RateLimitException(TimeUnit.NANOSECONDS.toMillis(consumptionProbe.getNanosToWaitForRefill()));
                }
                return joinPoint.proceed();
            }
        }
        throw new RateLimitException(Long.MAX_VALUE);
    }

    @Around("@annotation(RateLimitWebSocket)")
    public Object rateLimitWebSocket(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        RateLimitWebSocket rateLimitAnnotation = methodSignature.getMethod().getAnnotation(RateLimitWebSocket.class);
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Principal) {
                Principal principal = (Principal)arg;
                User user = userService.getUserFromWebSocket(principal);
                ConsumptionProbe consumptionProbe = RateLimitService.RateLimiter.rateLimit(user, rateLimitAnnotation.value());
                if (!consumptionProbe.isConsumed()) {
                    throw new RateLimitException(TimeUnit.NANOSECONDS.toMillis(consumptionProbe.getNanosToWaitForRefill()));
                }
                return joinPoint.proceed();
            }
        }
        throw new RateLimitException(Long.MAX_VALUE);
    }
}
