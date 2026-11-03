package com.dgmoonlabs.cms.domain.chatbot.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRateLimiterTest {
    @Test
    void eachVisitorGetsALimitedNumberOfQuestionsPerWindow() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-11-03T00:00:00Z"));
        ChatRateLimiter limiter = new ChatRateLimiter(new Clock() {
            @Override
            public ZoneOffset getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(final java.time.ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return now.get();
            }
        });

        for (int i = 0; i < 3; i++) {
            assertThat(limiter.tryAcquire("1.1.1.1", 3)).isTrue();
        }
        assertThat(limiter.tryAcquire("1.1.1.1", 3)).isFalse();
        assertThat(limiter.tryAcquire("2.2.2.2", 3)).isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1", 5)).isTrue();

        now.set(now.get().plus(ChatRateLimiter.WINDOW).plusSeconds(1));
        assertThat(limiter.tryAcquire("1.1.1.1", 3)).isTrue();
    }
}
