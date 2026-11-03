package com.dgmoonlabs.cms.domain.chatbot.dto;

import com.dgmoonlabs.cms.domain.chatbot.constant.ChatbotProvider;
import com.dgmoonlabs.cms.domain.chatbot.constant.DefaultInstructions;
import com.dgmoonlabs.cms.domain.chatbot.entity.ChatbotSetting;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class ChatbotDtos {
    private ChatbotDtos() {
    }

    public record ChatbotSettingRequest(
            boolean enabled,
            @NotNull ChatbotProvider provider,
            Long apiKeyId,
            @Size(max = 100) String model,
            @NotBlank @Size(max = 20_000) String instructions,
            @Size(max = 500) String welcomeMessage,
            @Min(1) @Max(100_000) Integer dailyLimitPerVisitor
    ) {
        public int dailyLimitOrDefault() {
            return dailyLimitPerVisitor == null ? ChatbotSetting.DEFAULT_DAILY_LIMIT : dailyLimitPerVisitor;
        }
    }

    public record ChatbotSettingResponse(boolean enabled, ChatbotProvider provider, Long apiKeyId, String model,
                                         String instructions, String welcomeMessage, int dailyLimitPerVisitor,
                                         String defaultInstructions) {
        public static ChatbotSettingResponse from(final ChatbotSetting setting) {
            return new ChatbotSettingResponse(setting.isEnabled(), setting.getProvider(), setting.getApiKeyId(),
                    setting.getModel(), setting.getInstructions(), setting.getWelcomeMessage(),
                    setting.getDailyLimitPerVisitor(), DefaultInstructions.TEXT);
        }
    }

    public record PublicChatbot(boolean enabled, String welcomeMessage) {
    }

    public enum Role {
        USER, ASSISTANT
    }

    public record ChatMessage(@NotNull Role role, @NotBlank @Size(max = 2000) String content) {
    }

    public record ChatRequest(@NotEmpty @Size(max = 20) List<@Valid ChatMessage> messages) {
    }

    public record ChatReply(String reply) {
    }
}
