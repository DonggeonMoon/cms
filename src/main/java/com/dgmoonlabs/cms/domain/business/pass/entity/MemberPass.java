package com.dgmoonlabs.cms.domain.business.pass.entity;

import com.dgmoonlabs.cms.domain.business.order.constant.PassUnit;
import com.dgmoonlabs.cms.domain.business.pass.constant.PassStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/** 회원이 산 이용권. 기간권은 만료일시로, 횟수권은 남은 횟수로 쓸 수 있는지 가린다. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MemberPass extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @Comment("이용권 상품 ID")
    private Long productId;

    @Column(nullable = false, length = 200)
    @Comment("이용권 상품명")
    private String productName;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("단위 (기간권·횟수권)")
    private PassUnit unit;

    @Column(nullable = false)
    @Comment("예약할 수 있는 예약 상품 ID")
    private Long programId;

    @Comment("전체 횟수 (횟수권)")
    private Integer totalCount;

    @Comment("남은 횟수 (횟수권)")
    private Integer remainingCount;

    @Comment("만료 일시 (기간권)")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Comment("발급한 주문 ID")
    private Long firstOrderId;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("상태")
    private PassStatus status;

    public boolean isUsableFor(final Long programId, final LocalDateTime now) {
        if (status != PassStatus.ACTIVE || !this.programId.equals(programId)) {
            return false;
        }
        if (expiresAt != null && now.isAfter(expiresAt)) {
            return false;
        }
        return unit != PassUnit.COUNT || (remainingCount != null && remainingCount > 0);
    }

    public void consume(final int quantity) {
        if (unit != PassUnit.COUNT) {
            return;
        }
        if (remainingCount == null || remainingCount < quantity) {
            throw new BusinessException(ErrorCode.PASS_NOT_USABLE);
        }
        this.remainingCount -= quantity;
        if (remainingCount == 0) {
            this.status = PassStatus.USED_UP;
        }
    }

    public void restore(final int quantity) {
        if (unit != PassUnit.COUNT) {
            return;
        }
        this.remainingCount = (remainingCount == null ? 0 : remainingCount) + quantity;
        if (status == PassStatus.USED_UP) {
            this.status = PassStatus.ACTIVE;
        }
    }

    public void cancel() {
        this.status = PassStatus.CANCELED;
    }
}
