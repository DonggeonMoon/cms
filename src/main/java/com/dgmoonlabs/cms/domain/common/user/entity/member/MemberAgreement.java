package com.dgmoonlabs.cms.domain.common.user.entity.member;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MemberAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("메일 수신 동의 여부")
    private boolean isMailReceptionAgreed;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("SMS 수신 동의 여부")
    private boolean isSmsReceptionAgreed;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("마케팅 활용 동의 여부")
    private boolean isMarketingUsageAgreed;

    public void update(final boolean isMailReceptionAgreed, final boolean isSmsReceptionAgreed, final boolean isMarketingUsageAgreed) {
        this.isMailReceptionAgreed = isMailReceptionAgreed;
        this.isSmsReceptionAgreed = isSmsReceptionAgreed;
        this.isMarketingUsageAgreed = isMarketingUsageAgreed;
    }
}
