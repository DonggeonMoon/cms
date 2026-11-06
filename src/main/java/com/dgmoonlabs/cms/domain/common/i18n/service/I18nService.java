package com.dgmoonlabs.cms.domain.common.i18n.service;

import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos.I18nSettingRequest;
import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos.I18nSettingResponse;
import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos.TranslationResponse;
import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos.TranslationSaveRequest;
import com.dgmoonlabs.cms.domain.common.i18n.entity.I18nSetting;
import com.dgmoonlabs.cms.domain.common.i18n.entity.Translation;
import com.dgmoonlabs.cms.domain.common.i18n.repository.I18nSettingRepository;
import com.dgmoonlabs.cms.domain.common.i18n.repository.TranslationRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class I18nService {
    private final I18nSettingRepository settingRepository;
    private final TranslationRepository translationRepository;

    @Transactional(readOnly = true)
    public I18nSettingResponse getSetting() {
        I18nSetting setting = settingRepository.findFirstByOrderByIdAsc().orElseGet(I18nSetting::defaults);
        return new I18nSettingResponse(setting.languageList(), setting.getDefaultLanguage());
    }

    @Transactional
    public I18nSettingResponse updateSetting(final I18nSettingRequest request) {
        List<String> languages = request.languages().stream().distinct().toList();
        if (!languages.contains(request.defaultLanguage())) {
            throw new BusinessException(ErrorCode.DEFAULT_LANGUAGE_NOT_SELECTED);
        }
        settingRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> settingRepository.save(I18nSetting.defaults()))
                .update(languages, request.defaultLanguage());
        return getSetting();
    }

    @Transactional(readOnly = true)
    public Map<String, String> getMessages(final String language) {
        Map<String, String> messages = new LinkedHashMap<>();
        translationRepository.findAllByLanguageOrderByMessageKeyAsc(language).forEach(t -> messages.put(t.getMessageKey(), t.getMessage()));
        return messages;
    }

    @Transactional(readOnly = true)
    public List<TranslationResponse> list(final String language) {
        return translationRepository.findAllByLanguageOrderByMessageKeyAsc(language).stream()
                .map(TranslationResponse::from).toList();
    }

    @Transactional
    public TranslationResponse create(final String language, final TranslationSaveRequest request) {
        if (translationRepository.existsByLanguageAndMessageKey(language, request.key())) {
            throw new BusinessException(ErrorCode.DUPLICATE_TRANSLATION_KEY);
        }
        Translation translation = translationRepository.save(
                Translation.builder().language(language).messageKey(request.key()).message(request.message()).build());
        return TranslationResponse.from(translation);
    }

    @Transactional
    public TranslationResponse update(final Long id, final TranslationSaveRequest request) {
        Translation translation = translationRepository.findById(id).orElseThrow(BusinessException::notFound);
        if (!translation.getMessageKey().equals(request.key())
                && translationRepository.existsByLanguageAndMessageKey(translation.getLanguage(), request.key())) {
            throw new BusinessException(ErrorCode.DUPLICATE_TRANSLATION_KEY);
        }
        translation.update(request.key(), request.message());
        return TranslationResponse.from(translation);
    }

    @Transactional
    public void delete(final Long id) {
        translationRepository.deleteById(id);
    }
}
