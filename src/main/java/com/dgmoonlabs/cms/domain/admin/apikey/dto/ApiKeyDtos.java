package com.dgmoonlabs.cms.domain.admin.apikey.dto;

import com.dgmoonlabs.cms.domain.admin.apikey.entity.ApiKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

public final class ApiKeyDtos {
    private ApiKeyDtos() {
    }

    @Builder
    public record ApiKeySaveRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 1000) String value,
            @Size(max = 255) String description
    ) {
        public String descriptionOrEmpty() {
            return description == null ? "" : description.trim();
        }
    }

    public record ApiKeyOption(Long id, String name) {
        public static ApiKeyOption from(final ApiKey apiKey) {
            return new ApiKeyOption(apiKey.getId(), apiKey.getName());
        }
    }

    public record ApiKeyResponse(Long id, String name, String value, String description, LocalDateTime modifiedAt) {
        public static ApiKeyResponse from(final ApiKey apiKey) {
            return new ApiKeyResponse(apiKey.getId(), apiKey.getName(), apiKey.getValue(), apiKey.getDescription(),
                    apiKey.getModifiedAt());
        }
    }
}
