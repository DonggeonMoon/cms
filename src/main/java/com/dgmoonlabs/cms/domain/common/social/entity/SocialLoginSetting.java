package com.dgmoonlabs.cms.domain.common.social.entity;

import com.dgmoonlabs.cms.domain.common.social.constant.SocialProvider;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.util.Objects;
import java.util.stream.Stream;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class SocialLoginSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("소셜 로그인 제공자")
    private SocialProvider provider;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("사용 여부")
    private boolean enabled;

    @Comment("Client ID (API 키 관리의 키 ID). 애플은 Services ID")
    private Long clientIdKeyId;

    @Comment("Client Secret (API 키 관리의 키 ID). 애플은 쓰지 않음")
    private Long clientSecretKeyId;

    @Column(length = 20)
    @Comment("애플 Team ID")
    private String appleTeamId;

    @Column(length = 20)
    @Comment("애플 Key ID")
    private String appleKeyId;

    @Comment("애플 개인 키 .p8 내용 (API 키 관리의 키 ID)")
    private Long applePrivateKeyKeyId;

    public static SocialLoginSetting empty(final SocialProvider provider) {
        return SocialLoginSetting.builder().provider(provider).build();
    }

    public void update(final boolean enabled, final Long clientIdKeyId, final Long clientSecretKeyId,
                       final String appleTeamId, final String appleKeyId, final Long applePrivateKeyKeyId) {
        this.enabled = enabled;
        this.clientIdKeyId = clientIdKeyId;
        this.clientSecretKeyId = clientSecretKeyId;
        this.appleTeamId = appleTeamId;
        this.appleKeyId = appleKeyId;
        this.applePrivateKeyKeyId = applePrivateKeyKeyId;
    }

    public boolean isConfigured() {
        if (clientIdKeyId == null) {
            return false;
        }
        return provider == SocialProvider.APPLE
                ? applePrivateKeyKeyId != null && notBlank(appleTeamId) && notBlank(appleKeyId)
                : provider == SocialProvider.KAKAO || clientSecretKeyId != null;
    }

    public boolean uses(final Long apiKeyId) {
        return Stream.of(clientIdKeyId, clientSecretKeyId, applePrivateKeyKeyId).anyMatch(id -> Objects.equals(id, apiKeyId));
    }

    private static boolean notBlank(final String value) {
        return value != null && !value.isBlank();
    }
}
