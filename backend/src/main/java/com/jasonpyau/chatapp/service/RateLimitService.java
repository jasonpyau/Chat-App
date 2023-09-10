package com.jasonpyau.chatapp.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jasonpyau.chatapp.entity.User;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Service
public class RateLimitService {

    public enum Token {
        CHEAP_TOKEN(1), DEFAULT_TOKEN(2), BIG_TOKEN(5), LARGE_TOKEN(15), EXPENSIVE_TOKEN(40);

        @Getter
        private final int value;

        Token(int value) {
            this.value = value;
        }
    }

    public static final RateLimitService RateLimiter = RateLimitService.builder()
                                                        .tokensPerInterval(200)
                                                        .intervalDuration(Duration.ofSeconds(60))
                                                        .maximumCacheSize(2000)
                                                        .cacheDuration(Duration.ofMinutes(10))
                                                        .build();

    private int tokensPerInterval;

    private Duration intervalDuration;

    private LoadingCache<User, Bucket> cache;

    private RateLimitService() {};

    @Builder(access = AccessLevel.PRIVATE)
    private RateLimitService(int tokensPerInterval, Duration intervalDuration, int maximumCacheSize, Duration cacheDuration) {
        this.tokensPerInterval = tokensPerInterval;
        this.intervalDuration = intervalDuration;
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(maximumCacheSize)
            .expireAfterAccess(cacheDuration)
            .build(new CacheLoader<User, Bucket>() {
                @Override
                public Bucket load(User key) {
                    return newBucket();
                }
            });
    }

    private Bandwidth getBandwidthLimit() {
        return Bandwidth.classic(tokensPerInterval, Refill.intervally(tokensPerInterval, intervalDuration));
    }

    private Bucket newBucket() {
        return Bucket.builder()
                    .addLimit(getBandwidthLimit())
                    .build();
    }

    public ConsumptionProbe rateLimit(User user, Token token) {
        Bucket bucket = cache.getUnchecked(user);
        return bucket.tryConsumeAndReturnRemaining(token.getValue());
    } 
}
