package com.dgmoonlabs.cms.domain.content.dto;

import com.dgmoonlabs.cms.domain.content.constant.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ContentSaveRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 500_000) String contentHtml,
        @NotNull ContentStatus status,
        @Size(max = 500) String memo
) {
    public String memoOrEmpty() {
        return memo == null ? "" : memo;
    }
}
