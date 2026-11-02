package com.dgmoonlabs.cms.domain.admin.config.dto;

import com.dgmoonlabs.cms.domain.admin.config.constant.OptionType;
import com.dgmoonlabs.cms.domain.admin.config.entity.Config;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ConfigSaveRequest(
        @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_.-]{1,100}$", message = "키는 영문, 숫자, _ . - 로 100자 이내여야 합니다.") String key,
        @Size(max = 2000) String value,
        @Size(max = 255) String description,
        @NotNull OptionType type,
        boolean isHidden
) {
    public String valueOrEmpty() {
        return value == null ? "" : value;
    }

    public String descriptionOrEmpty() {
        return description == null ? "" : description;
    }

    public Config toEntity() {
        return Config.builder()
                .key(key)
                .value(valueOrEmpty())
                .description(descriptionOrEmpty())
                .type(type)
                .isHidden(isHidden)
                .build();
    }
}
