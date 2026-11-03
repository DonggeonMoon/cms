package com.dgmoonlabs.cms.domain.common.social.entity;

import com.dgmoonlabs.cms.domain.common.social.constant.SocialProvider;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_social_account", columnNames = {"provider", "providerUserId"}),
        indexes = @Index(name = "idx_social_account_member", columnList = "memberId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class SocialAccount extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("소셜 로그인 제공자")
    private SocialProvider provider;

    @Column(nullable = false, length = 255)
    @Comment("제공자의 사용자 식별값")
    private String providerUserId;
}
