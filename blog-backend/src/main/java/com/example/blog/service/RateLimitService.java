package com.example.blog.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    private final int maxLoginAttempts;
    private final int lockoutDurationMinutes;
    private final Duration attemptWindow;
    private final int globalIpMaxAttempts;
    private final Duration globalIpLockoutSeconds;

    private final Cache<String, AtomicInteger> loginAttempts;
    private final Cache<String, Long> lockedOutAccounts;
    private final ConcurrentHashMap<String, AtomicInteger> globalIpAttempts;
    private final Cache<String, Long> globalIpLockouts;

    public RateLimitService(
            @Value("${rate-limit.max-login-attempts:5}") int maxLoginAttempts,
            @Value("${rate-limit.lockout-duration-minutes:15}") int lockoutDurationMinutes,
            @Value("${rate-limit.attempt-window-minutes:5}") int attemptWindowMinutes,
            @Value("${rate-limit.global-ip-max-attempts:20}") int globalIpMaxAttempts,
            @Value("${rate-limit.global-ip-lockout-seconds:60}") int globalIpLockoutSeconds) {
        this.maxLoginAttempts = maxLoginAttempts;
        this.lockoutDurationMinutes = lockoutDurationMinutes;
        this.attemptWindow = Duration.ofMinutes(attemptWindowMinutes);
        this.globalIpMaxAttempts = globalIpMaxAttempts;
        this.globalIpLockoutSeconds = Duration.ofSeconds(globalIpLockoutSeconds);

        this.loginAttempts = Caffeine.newBuilder()
                .expireAfterAccess(this.attemptWindow)
                .maximumSize(10000)
                .build();

        this.lockedOutAccounts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(this.lockoutDurationMinutes))
                .maximumSize(10000)
                .build();

        this.globalIpAttempts = new ConcurrentHashMap<>();

        this.globalIpLockouts = Caffeine.newBuilder()
                .expireAfterWrite(this.globalIpLockoutSeconds)
                .maximumSize(1000)
                .build();
    }

    public boolean isLockedOut(String identifier) {
        Long lockoutEnd = lockedOutAccounts.getIfPresent(identifier);
        if (lockoutEnd != null) {
            if (System.currentTimeMillis() < lockoutEnd) {
                return true;
            } else {
                lockedOutAccounts.invalidate(identifier);
                loginAttempts.invalidate(identifier);
            }
        }
        return false;
    }

    public long getRemainingLockoutTime(String identifier) {
        Long lockoutEnd = lockedOutAccounts.getIfPresent(identifier);
        if (lockoutEnd != null && System.currentTimeMillis() < lockoutEnd) {
            return (lockoutEnd - System.currentTimeMillis()) / 1000;
        }
        return 0;
    }

    public void recordFailedLogin(String identifier) {
        AtomicInteger attempts = loginAttempts.get(identifier, k -> new AtomicInteger(0));
        int currentAttempts = attempts.incrementAndGet();

        if (currentAttempts >= maxLoginAttempts) {
            lockedOutAccounts.put(identifier, System.currentTimeMillis() + (lockoutDurationMinutes * 60 * 1000L));
            loginAttempts.invalidate(identifier);
        }
    }

    public void clearFailedLoginAttempts(String identifier) {
        loginAttempts.invalidate(identifier);
        lockedOutAccounts.invalidate(identifier);
    }

    public int getFailedLoginAttempts(String identifier) {
        AtomicInteger attempts = loginAttempts.getIfPresent(identifier);
        return attempts != null ? attempts.get() : 0;
    }

    public boolean isGlobalRateLimited(String ip) {
        Long lockoutEnd = globalIpLockouts.getIfPresent(ip);
        if (lockoutEnd != null) {
            if (System.currentTimeMillis() < lockoutEnd) {
                return true;
            } else {
                globalIpLockouts.invalidate(ip);
                globalIpAttempts.remove(ip);
            }
        }
        return false;
    }

    public void recordGlobalIpFailure(String ip) {
        AtomicInteger attempts = globalIpAttempts.computeIfAbsent(ip, k -> new AtomicInteger(0));
        int currentAttempts = attempts.incrementAndGet();

        if (currentAttempts > globalIpMaxAttempts) {
            globalIpLockouts.put(ip, System.currentTimeMillis() + globalIpLockoutSeconds.toMillis());
            globalIpAttempts.remove(ip);
        }
    }

    public void clearGlobalIpFailure(String ip) {
        globalIpAttempts.remove(ip);
        globalIpLockouts.invalidate(ip);
    }
}