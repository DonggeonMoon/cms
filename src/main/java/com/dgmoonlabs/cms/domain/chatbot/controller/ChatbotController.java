package com.dgmoonlabs.cms.domain.chatbot.controller;

import com.dgmoonlabs.cms.domain.chatbot.dto.ChatbotDtos.*;
import com.dgmoonlabs.cms.domain.chatbot.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;

    @GetMapping("/api/chatbot")
    public ResponseEntity<PublicChatbot> publicSetting() {
        return ResponseEntity.ok(chatbotService.getPublic());
    }

    @PostMapping("/api/chatbot/messages")
    public ResponseEntity<ChatReply> chat(@RequestBody @Valid final ChatRequest request, final HttpServletRequest httpRequest) {
        return ResponseEntity.ok(chatbotService.chat(request, httpRequest.getRemoteAddr()));
    }

    @GetMapping("/api/admin/chatbot")
    public ResponseEntity<ChatbotSettingResponse> setting() {
        return ResponseEntity.ok(chatbotService.getSetting());
    }

    @PutMapping("/api/admin/chatbot")
    public ResponseEntity<ChatbotSettingResponse> update(@RequestBody @Valid final ChatbotSettingRequest request) {
        return ResponseEntity.ok(chatbotService.updateSetting(request));
    }
}
