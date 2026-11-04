package com.dgmoonlabs.cms.domain.common.coupon.entity;

import com.dgmoonlabs.cms.domain.common.coupon.constant.CouponIssueStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_coupon_issue_member", columnList = "memberId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CouponIssue extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("쿠폰 ID")
    private Long couponId;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("상태")
    private CouponIssueStatus status = CouponIssueStatus.AVAILABLE;

    @Comment("사용 기한")
    private LocalDateTime expiresAt;

    @Comment("사용한 주문 ID")
    private Long orderId;

    @Comment("사용 일시")
    private LocalDateTime usedAt;

    public boolean isUsableAt(final LocalDateTime now) {
        return status == CouponIssueStatus.AVAILABLE && (expiresAt == null || expiresAt.isAfter(now));
    }

    public void use(final Long orderId) {
        if (!isUsableAt(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.COUPON_NOT_USABLE);
        }
        this.status = CouponIssueStatus.USED;
        this.orderId = orderId;
        this.usedAt = LocalDateTime.now();
    }

    /** 주문이 통째로 취소되면 쿠폰을 되살린다. 그사이 기한이 지났으면 소멸로 둔다. */
    public void restore() {
        this.status = expiresAt != null && !expiresAt.isAfter(LocalDateTime.now()) ? CouponIssueStatus.EXPIRED : CouponIssueStatus.AVAILABLE;
        this.orderId = null;
        this.usedAt = null;
    }

    public void expire() {
        this.status = CouponIssueStatus.EXPIRED;
    }
}
