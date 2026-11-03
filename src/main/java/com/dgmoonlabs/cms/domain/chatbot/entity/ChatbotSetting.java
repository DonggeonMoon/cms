package com.dgmoonlabs.cms.domain.chatbot.entity;

import com.dgmoonlabs.cms.domain.chatbot.constant.ChatbotProvider;
import com.dgmoonlabs.cms.domain.chatbot.constant.DefaultInstructions;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.util.Objects;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ChatbotSetting extends BaseEntity {
    public static final int DEFAULT_DAILY_LIMIT = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("챗봇 사용 여부")
    private boolean enabled;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("AI 제공사")
    private ChatbotProvider provider = ChatbotProvider.CLAUDE;

    @Comment("API 키 (API 키 관리의 키 ID)")
    private Long apiKeyId;

    @Comment("모델 ID")
    private String model;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "text")
    @Comment("챗봇 지침 (시스템 프롬프트)")
    private String instructions = DefaultInstructions.TEXT;

    @Comment("첫 인사말")
    private String welcomeMessage;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("1000")
    @Comment("방문자(IP)당 하루 질문 수 한도")
    private int dailyLimitPerVisitor = DEFAULT_DAILY_LIMIT;

    public static ChatbotSetting defaults() {
        return ChatbotSetting.builder().build();
    }

    public void update(final boolean enabled, final ChatbotProvider provider, final Long apiKeyId, final String model,
                       final String instructions, final String welcomeMessage, final int dailyLimitPerVisitor) {
        this.enabled = enabled;
        this.provider = provider;
        this.apiKeyId = apiKeyId;
        this.model = model;
        this.instructions = instructions;
        this.welcomeMessage = welcomeMessage;
        this.dailyLimitPerVisitor = dailyLimitPerVisitor;
    }

    public boolean uses(final Long candidateApiKeyId) {
        return Objects.equals(apiKeyId, candidateApiKeyId);
    }
}
