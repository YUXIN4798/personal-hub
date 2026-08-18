package com.tianshi.hub.service;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final Clock clock;
    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService() {
        this(Clock.systemDefaultZone());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public boolean isLocked(String key) {
        pruneExpiredLocks();
        AttemptState state = attempts.get(key);
        return state != null && state.lockedUntil != null && state.lockedUntil.isAfter(now());
    }

    public void recordFailure(String key) {
        Instant now = now();
        attempts.compute(key, (ignored, state) -> {
            AttemptState next = state == null ? new AttemptState(0, null) : state;
            int failures = next.failures + 1;
            Instant lockedUntil = failures >= MAX_FAILURES ? now.plus(LOCK_DURATION) : null;
            return new AttemptState(failures, lockedUntil);
        });
    }

    public void clear(String key) {
        attempts.remove(key);
    }

    public int remainingMinutes(String key) {
        AttemptState state = attempts.get(key);
        if (state == null || state.lockedUntil == null) {
            return 0;
        }
        long seconds = Duration.between(now(), state.lockedUntil).toSeconds();
        return (int) Math.max(1, Math.ceil(seconds / 60.0));
    }

    // Lightweight in-memory protection: state is cleared on restart; multi-instance deployments need centralized storage.
    private void pruneExpiredLocks() {
        Instant now = now();
        Iterator<Map.Entry<String, AttemptState>> iterator = attempts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AttemptState> entry = iterator.next();
            AttemptState state = entry.getValue();
            if (state.lockedUntil != null && !state.lockedUntil.isAfter(now)) {
                iterator.remove();
            }
        }
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private record AttemptState(int failures, Instant lockedUntil) {
    }
}
