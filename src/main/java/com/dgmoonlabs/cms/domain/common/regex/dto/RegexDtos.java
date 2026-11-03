package com.dgmoonlabs.cms.domain.common.regex.dto;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.entity.RegexRule;
import com.dgmoonlabs.cms.domain.common.regex.validation.ValidRegex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class RegexDtos {
    private RegexDtos() {
    }

    public record RegexSaveRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 500) @ValidRegex String pattern,
            @NotBlank @Size(max = 200) String message,
            @Size(max = 100) String hint,
            @Size(max = 500) String description
    ) {
    }

    public record RegexResponse(Long id, String name, String pattern, String message, String hint, String description,
                                List<String> usedBy, LocalDateTime modifiedAt) {
        public static RegexResponse of(final RegexRule rule, final List<RegexTarget> targets) {
            return new RegexResponse(rule.getId(), rule.getName(), rule.getPattern(), rule.getMessage(), rule.getHint(),
                    rule.getDescription(), targets.stream().map(RegexTarget::getLabel).toList(), rule.getModifiedAt());
        }
    }

    public record RegexOption(Long id, String name, String pattern) {
    }

    public record PublicRegex(RegexTarget target, String pattern, String message, String hint) {
    }

    public record BindingRequest(Long regexRuleId) {
    }

    public record RegexTestRequest(@NotBlank @Size(max = 500) String pattern, @NotNull @Size(max = 500) String value) {
    }

    public record RegexTestResult(boolean valid, boolean matches, String error) {
    }
}
