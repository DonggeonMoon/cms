package com.dgmoonlabs.cms.domain.chatbot.client;

import com.dgmoonlabs.cms.domain.chatbot.constant.ChatbotProvider;
import com.dgmoonlabs.cms.domain.chatbot.dto.ChatbotDtos.ChatMessage;
import com.dgmoonlabs.cms.domain.chatbot.dto.ChatbotDtos.Role;

import java.util.List;

public interface ChatModelClient {
    String complete(ChatbotProvider provider, String apiKey, String model, String system, List<ChatMessage> messages);

    static String roleName(final Role role, final String assistantName) {
        return role == Role.USER ? "user" : assistantName;
    }
}
