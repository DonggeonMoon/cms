package com.dgmoonlabs.cms.domain.chatbot.service;

import com.dgmoonlabs.cms.domain.chatbot.client.ChatModelClient;
import com.dgmoonlabs.cms.domain.chatbot.dto.ChatbotDtos.*;
import com.dgmoonlabs.cms.domain.chatbot.entity.ChatbotSetting;
import com.dgmoonlabs.cms.domain.chatbot.repository.ChatbotSettingRepository;
import com.dgmoonlabs.cms.domain.admin.apikey.service.ApiKeyService;
import com.dgmoonlabs.cms.domain.admin.site.service.SiteService;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private final ChatbotSettingRepository repository;
    private final ApiKeyService apiKeyService;
    private final SiteService siteService;
    private final ChatModelClient client;
    private final ChatRateLimiter rateLimiter;

    @Transactional(readOnly = true)
    public ChatbotSettingResponse getSetting() {
        return ChatbotSettingResponse.from(current());
    }

    @Transactional
    public ChatbotSettingResponse updateSetting(final ChatbotSettingRequest request) {
        apiKeyService.requireExists(request.apiKeyId());
        String model = blankToNull(request.model());
        if (request.enabled() && (request.apiKeyId() == null || model == null)) {
            throw new BusinessException(ErrorCode.CHATBOT_NOT_CONFIGURED);
        }
        ChatbotSetting setting = repository.findFirstByOrderByIdAsc().orElseGet(() -> repository.save(ChatbotSetting.defaults()));
        setting.update(request.enabled(), request.provider(), request.apiKeyId(), model, request.instructions(),
                blankToNull(request.welcomeMessage()), request.dailyLimitOrDefault());
        return ChatbotSettingResponse.from(setting);
    }

    @Transactional(readOnly = true)
    public PublicChatbot getPublic() {
        ChatbotSetting setting = current();
        return new PublicChatbot(isReady(setting), isReady(setting) ? setting.getWelcomeMessage() : null);
    }

    @Transactional(readOnly = true)
    public ChatReply chat(final ChatRequest request, final String visitor) {
        ChatbotSetting setting = current();
        if (!isReady(setting)) {
            throw new BusinessException(ErrorCode.CHATBOT_DISABLED);
        }
        List<ChatMessage> messages = request.messages();
        if (messages.getLast().role() != Role.USER) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (!rateLimiter.tryAcquire(visitor, setting.getDailyLimitPerVisitor())) {
            throw new BusinessException(ErrorCode.CHATBOT_RATE_LIMITED);
        }
        String apiKey = apiKeyService.valueOf(setting.getApiKeyId());
        if (apiKey == null) {
            throw new BusinessException(ErrorCode.CHATBOT_NOT_CONFIGURED);
        }
        String system = "사이트 이름: " + siteService.getDefaultSite().name() + "\n\n" + setting.getInstructions();
        return new ChatReply(client.complete(setting.getProvider(), apiKey, setting.getModel(), system, messages));
    }

    private boolean isReady(final ChatbotSetting setting) {
        return setting.isEnabled() && setting.getApiKeyId() != null && setting.getModel() != null;
    }

    private ChatbotSetting current() {
        return repository.findFirstByOrderByIdAsc().orElseGet(ChatbotSetting::defaults);
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
