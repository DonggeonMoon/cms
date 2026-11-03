package com.dgmoonlabs.cms.domain.common.guestposting;

import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class GuestPostingSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("질문답변 비회원 등록 허용")
    private boolean qnaAllowed = true;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("신고 비회원 접수 허용")
    private boolean reportAllowed = true;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("질문답변 비회원 연락처 수집")
    private boolean qnaCollectPhone = false;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("질문답변 비회원 이메일 수집")
    private boolean qnaCollectEmail = true;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("신고 비회원 연락처 수집")
    private boolean reportCollectPhone = true;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("신고 비회원 이메일 수집")
    private boolean reportCollectEmail = false;

    public static GuestPostingSetting defaults() {
        return GuestPostingSetting.builder().build();
    }

    public GuestPostingPolicy policy(final SiteFeature feature) {
        return switch (feature) {
            case QNA -> new GuestPostingPolicy(qnaAllowed, qnaCollectPhone, qnaCollectEmail);
            case REPORT -> new GuestPostingPolicy(reportAllowed, reportCollectPhone, reportCollectEmail);
            default -> throw new IllegalArgumentException("비회원 등록 설정이 없는 기능입니다: " + feature);
        };
    }

    public void change(final SiteFeature feature, final GuestPostingPolicy policy) {
        switch (feature) {
            case QNA -> {
                qnaAllowed = policy.allowed();
                qnaCollectPhone = policy.collectPhone();
                qnaCollectEmail = policy.collectEmail();
            }
            case REPORT -> {
                reportAllowed = policy.allowed();
                reportCollectPhone = policy.collectPhone();
                reportCollectEmail = policy.collectEmail();
            }
            default -> throw new IllegalArgumentException("비회원 등록 설정이 없는 기능입니다: " + feature);
        }
    }
}
