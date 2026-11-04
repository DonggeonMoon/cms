package com.dgmoonlabs.cms.domain.application.entity;

import com.dgmoonlabs.cms.domain.application.constant.FormFieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FormField(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{1,40}$") String key,
        @NotBlank @Size(max = 100) String label,
        @NotNull FormFieldType type,
        boolean required,
        @Size(max = 50) List<@NotBlank @Size(max = 100) String> options,
        @Size(max = 300) String help
) {
    public List<String> optionsOrEmpty() {
        return options == null ? List.of() : options;
    }
}
