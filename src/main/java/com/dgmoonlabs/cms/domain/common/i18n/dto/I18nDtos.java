package com.dgmoonlabs.cms.domain.common.i18n.dto;

import com.dgmoonlabs.cms.domain.common.i18n.entity.Translation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class I18nDtos {
    public static final String LANGUAGE_CODE = "^[a-z]{2}(-[A-Z]{2})?$";
    public static final String TRANSLATION_KEY = "^[A-Za-z0-9_.-]{1,200}$";

    private I18nDtos() {
    }

    public record I18nSettingResponse(List<String> languages, String defaultLanguage) {
    }

    public record I18nSettingRequest(
            @NotNull @Size(min = 1, max = 20) List<@Pattern(regexp = LANGUAGE_CODE) String> languages,
            @NotBlank @Pattern(regexp = LANGUAGE_CODE) String defaultLanguage
    ) {
    }

    public record TranslationSaveRequest(
            @NotBlank @Pattern(regexp = TRANSLATION_KEY) String key,
            @NotBlank @Size(max = 5000) String message
    ) {
    }

    public record TranslationResponse(Long id, String key, String message) {
        public static TranslationResponse from(final Translation translation) {
            return new TranslationResponse(translation.getId(), translation.getMessageKey(), translation.getMessage());
        }
    }
}
