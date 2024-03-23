package com.dgmoonlabs.cms.domain.common.user.entity.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("메일 수신 동의 여부")
    private boolean isMailReceptionAgreed;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("SMS 수신 동의 여부")
    private boolean isSmsReceptionAgreed;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("마케팅 활용 동의 여부")
    private boolean isMarketingUsageAgreed;
}
