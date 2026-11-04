package com.dgmoonlabs.cms.domain.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ContentWorkflowDtos {
    private ContentWorkflowDtos() {
    }

    /** publishAt 을 비우면 승인하는 즉시 발행한다. */
    public record ReviewRequest(LocalDateTime publishAt) {
    }

    public record RejectRequest(@NotBlank @Size(max = 1000) String comment) {
    }
}
