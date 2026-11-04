package com.dgmoonlabs.cms.domain.business.category.entity;

import com.dgmoonlabs.cms.domain.business.category.constant.AttributeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CategoryAttribute(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{1,40}$") String key,
        @NotBlank @Size(max = 50) String label,
        @NotNull AttributeType type,
        @Size(max = 50) List<@NotBlank @Size(max = 50) String> options,
        @Size(max = 20) String unit,
        boolean filterable
) {
    public List<String> optionsOrEmpty() {
        return options == null ? List.of() : options;
    }
}
