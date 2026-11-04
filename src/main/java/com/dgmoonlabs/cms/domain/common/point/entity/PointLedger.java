package com.dgmoonlabs.cms.domain.common.point.entity;

import com.dgmoonlabs.cms.domain.common.point.constant.PointType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_point_ledger_member", columnList = "memberId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PointLedger extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("구분")
    private PointType type;

    @Column(nullable = false)
    @Comment("증감 포인트 (적립 +, 사용·소멸 -)")
    private long amount;

    @Column(nullable = false)
    @Comment("적립 건에서 아직 쓰지 않은 포인트 (차감 건은 0)")
    private long remaining;

    @Comment("적립 건의 소멸 일시")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Comment("처리 후 잔액")
    private long balanceAfter;

    @Comment("관련 주문 ID")
    private Long orderId;

    @Column(length = 200)
    @Comment("메모")
    private String memo;

    public boolean isUsable(final LocalDateTime now) {
        return remaining > 0 && (expiresAt == null || expiresAt.isAfter(now));
    }

    public long take(final long wanted) {
        long taken = Math.min(remaining, wanted);
        remaining -= taken;
        return taken;
    }
}
