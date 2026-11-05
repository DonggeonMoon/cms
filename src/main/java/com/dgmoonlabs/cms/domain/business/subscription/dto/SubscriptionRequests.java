package com.dgmoonlabs.cms.domain.business.subscription.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public final class SubscriptionRequests {
    private SubscriptionRequests() {
    }

    /** resumeOn 을 비우면 직접 다시 시작할 때까지 멈춘다. */
    public record PauseRequest(LocalDate resumeOn) {
    }

    public record PlanChangeRequest(@NotNull Long productId) {
    }
}
