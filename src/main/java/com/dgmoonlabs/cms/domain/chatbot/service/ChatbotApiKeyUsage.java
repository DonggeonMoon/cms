package com.dgmoonlabs.cms.domain.chatbot.service;

import com.dgmoonlabs.cms.domain.chatbot.repository.ChatbotSettingRepository;
import com.dgmoonlabs.cms.domain.admin.apikey.service.ApiKeyUsage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatbotApiKeyUsage implements ApiKeyUsage {
    private final ChatbotSettingRepository repository;

    @Override
    public boolean isUsing(final Long apiKeyId) {
        return repository.findFirstByOrderByIdAsc().map(setting -> setting.uses(apiKeyId)).orElse(false);
    }
}
