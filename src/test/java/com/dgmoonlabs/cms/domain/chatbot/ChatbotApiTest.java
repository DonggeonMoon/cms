package com.dgmoonlabs.cms.domain.chatbot;

import com.dgmoonlabs.cms.domain.admin.apikey.entity.ApiKey;
import com.dgmoonlabs.cms.domain.admin.apikey.repository.ApiKeyRepository;
import com.dgmoonlabs.cms.domain.chatbot.client.ChatModelClient;
import com.dgmoonlabs.cms.domain.chatbot.constant.ChatbotProvider;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatbotApiTest extends ApiTestSupport {
    @MockitoBean
    ChatModelClient client;

    @Autowired
    ApiKeyRepository apiKeyRepository;

    private static final String QUESTION = """
            {"messages":[{"role":"USER","content":"회원가입은 어떻게 하나요?"}]}
            """;

    @Test
    void theChatbotAnswersOnlyOnceTurnedOnWithAKeyAndModel() throws Exception {
        MockHttpSession admin = admin();
        getJson("/api/chatbot", null).andExpect(jsonPath("$.enabled").value(false));
        getJson("/api/admin/chatbot", admin)
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.instructions").value(org.hamcrest.Matchers.containsString("프롬프트 인젝션")))
                .andExpect(jsonPath("$.dailyLimitPerVisitor").value(1000));
        postJson("/api/chatbot/messages", null, QUESTION).andExpect(jsonPath("$.code").value("CHATBOT_DISABLED"));

        putJson("/api/admin/chatbot", admin, """
                {"enabled":true,"provider":"CLAUDE","instructions":"친절하게 답하세요."}
                """).andExpect(jsonPath("$.code").value("CHATBOT_NOT_CONFIGURED"));

        long keyId = apiKeyRepository.save(ApiKey.builder().name("Claude").value("sk-test").description("").build()).getId();
        putJson("/api/admin/chatbot", admin, """
                {"enabled":true,"provider":"CLAUDE","apiKeyId":%d,"model":"claude-haiku-5-5",
                 "instructions":"친절하게 답하세요.","welcomeMessage":"무엇을 도와드릴까요?","dailyLimitPerVisitor":2}
                """.formatted(keyId)).andExpect(jsonPath("$.dailyLimitPerVisitor").value(2));
        getJson("/api/chatbot", null)
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.welcomeMessage").value("무엇을 도와드릴까요?"));

        when(client.complete(eq(ChatbotProvider.CLAUDE), eq("sk-test"), eq("claude-haiku-5-5"), any(), anyList()))
                .thenReturn("상단의 회원가입 메뉴에서 가입할 수 있습니다.");
        postJson("/api/chatbot/messages", null, QUESTION)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("상단의 회원가입 메뉴에서 가입할 수 있습니다."));

        ArgumentCaptor<String> system = ArgumentCaptor.forClass(String.class);
        verify(client).complete(any(), any(), any(), system.capture(), anyList());
        assertThat(system.getValue()).startsWith("사이트 이름: ").contains("친절하게 답하세요.");

        postJson("/api/chatbot/messages", null, """
                {"messages":[{"role":"ASSISTANT","content":"안녕하세요"}]}
                """).andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        postJson("/api/chatbot/messages", null, QUESTION).andExpect(status().isOk());
        postJson("/api/chatbot/messages", null, QUESTION).andExpect(jsonPath("$.code").value("CHATBOT_RATE_LIMITED"));
    }
}
