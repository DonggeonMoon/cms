package com.dgmoonlabs.cms.domain.chatbot.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 메모리에 세므로 인스턴스가 여러 대면 각자 센다. 그때는 Redis나 DB로 옮긴다.
@Component
public class ChatRateLimiter {
    static final Duration WINDOW = Duration.ofDays(1);

    private final Map<String, Deque<Instant>> requests = new ConcurrentHashMap<>();
    private final Clock clock;

    public ChatRateLimiter() {
        this(Clock.systemUTC());
    }

    ChatRateLimiter(final Clock clock) {
        this.clock = clock;
    }

    public boolean tryAcquire(final String visitor, final int limit) {
        Instant now = clock.instant();
        Deque<Instant> times = requests.computeIfAbsent(visitor, key -> new ArrayDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && !times.peekFirst().isAfter(now.minus(WINDOW))) {
                times.pollFirst();
            }
            if (times.size() >= limit) {
                return false;
            }
            times.addLast(now);
            return true;
        }
    }
}
