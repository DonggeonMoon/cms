package com.dgmoonlabs.cms.domain.common.i18n.service;

import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos.*;
import com.dgmoonlabs.cms.domain.common.i18n.entity.I18nSetting;
import com.dgmoonlabs.cms.domain.common.i18n.entity.Translation;
import com.dgmoonlabs.cms.domain.common.i18n.repository.I18nSettingRepository;
import com.dgmoonlabs.cms.domain.common.i18n.repository.TranslationRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class I18nService {
    private static final Pattern KEY = Pattern.compile("^[A-Za-z0-9_.-]{1,200}$");
    private static final int MAX_MESSAGE = 5000;

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
    public BundleResponse export(final String language, final BundleFormat format) {
        Map<String, String> messages = getMessages(language);
        return new BundleResponse(language, format, format == BundleFormat.YAML ? toYaml(messages) : toProperties(messages), messages.size());
    }

    @Transactional
    public BundleResponse replace(final String language, final BundleRequest request) {
        Map<String, String> messages = request.format() == BundleFormat.YAML ? parseYaml(request.text()) : parseProperties(request.text());
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            if (!KEY.matcher(entry.getKey()).matches() || entry.getValue().length() > MAX_MESSAGE) {
                throw new BusinessException(ErrorCode.INVALID_TRANSLATION_BUNDLE);
            }
        }
        translationRepository.deleteAllByLanguage(language);
        translationRepository.flush();
        messages.forEach((key, message) -> translationRepository.save(
                Translation.builder().language(language).messageKey(key).message(message).build()));
        return export(language, request.format());
    }

    static Map<String, String> parseProperties(final String text) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(text));
        } catch (IOException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TRANSLATION_BUNDLE, e);
        }
        Map<String, String> messages = new TreeMap<>();
        properties.stringPropertyNames().forEach(key -> messages.put(key, properties.getProperty(key)));
        return messages;
    }

    static Map<String, String> parseYaml(final String text) {
        Object loaded;
        try {
            loaded = new Yaml(new SafeConstructor(new LoaderOptions())).load(text);
        } catch (YAMLException e) {
            throw new BusinessException(ErrorCode.INVALID_TRANSLATION_BUNDLE, e);
        }
        Map<String, String> messages = new TreeMap<>();
        if (loaded == null) {
            return messages;
        }
        if (!(loaded instanceof Map<?, ?> root)) {
            throw new BusinessException(ErrorCode.INVALID_TRANSLATION_BUNDLE);
        }
        flatten("", root, messages);
        return messages;
    }

    private static void flatten(final String prefix, final Map<?, ?> map, final Map<String, String> into) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = prefix + entry.getKey();
            if (entry.getValue() instanceof Map<?, ?> child) {
                flatten(key + ".", child, into);
            } else if (entry.getValue() != null) {
                into.put(key, String.valueOf(entry.getValue()));
            }
        }
    }

    static String toProperties(final Map<String, String> messages) {
        StringBuilder out = new StringBuilder();
        messages.forEach((key, message) -> out.append(key).append(" = ").append(escapeProperty(message)).append('\n'));
        return out.toString();
    }

    private static String escapeProperty(final String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(i == 0 && (c == ' ' || c == '#' || c == '!') ? "\\" + c : String.valueOf(c));
            }
        }
        return out.toString();
    }

    static String toYaml(final Map<String, String> messages) {
        if (messages.isEmpty()) {
            return "";
        }
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(true);
        return new Yaml(options).dump(new LinkedHashMap<>(messages));
    }
}
