package com.dgmoonlabs.cms.domain.common.i18n.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class I18nDtos {
    public static final String LANGUAGE_CODE = "^[a-z]{2}(-[A-Z]{2})?$";

    private I18nDtos() {
    }

    public enum BundleFormat {
        PROPERTIES, YAML
    }

    public record I18nSettingResponse(List<String> languages, String defaultLanguage) {
    }

    public record I18nSettingRequest(
            @NotNull @Size(min = 1, max = 20) List<@Pattern(regexp = LANGUAGE_CODE) String> languages,
            @NotBlank @Pattern(regexp = LANGUAGE_CODE) String defaultLanguage
    ) {
    }

    public record BundleRequest(@NotNull BundleFormat format, @NotNull @Size(max = 500_000) String text) {
    }

    public record BundleResponse(String language, BundleFormat format, String text, int count) {
    }
}
