package com.dgmoonlabs.cms.domain.chatbot.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatbotProvider {
    OPENAI("ChatGPT (OpenAI)"),
    GEMINI("Gemini (Google)"),
    CLAUDE("Claude (Anthropic)");

    private final String label;
}
