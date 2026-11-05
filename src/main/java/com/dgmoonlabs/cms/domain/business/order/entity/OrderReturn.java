package com.dgmoonlabs.cms.domain.business.order.entity;

import com.dgmoonlabs.cms.domain.business.order.constant.OrderReturnStatus;
import com.dgmoonlabs.cms.domain.business.order.constant.OrderReturnType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_order_return_order", columnList = "orderId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class OrderReturn extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("주문 ID")
    private Long orderId;

    @Column(nullable = false)
    @Comment("신청한 회원 ID")
    private Long memberId;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("반품 / 교환")
    private OrderReturnType type;

    @Column(nullable = false, columnDefinition = "text")
    @Comment("신청 이유")
    private String reason;

    @Comment("교환 희망 옵션(조합) ID")
    private Long exchangeOptionId;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'REQUESTED'")
    @Enumerated(EnumType.STRING)
    @Comment("처리 상태")
    private OrderReturnStatus status = OrderReturnStatus.REQUESTED;

    @Column(columnDefinition = "text")
    @Comment("관리자 메모")
    private String adminMemo;

    @Comment("처리 일시")
    private LocalDateTime processedAt;

    public void approve(final String adminMemo) {
        if (status != OrderReturnStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = OrderReturnStatus.APPROVED;
        this.adminMemo = adminMemo;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(final String adminMemo) {
        if (status != OrderReturnStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = OrderReturnStatus.REJECTED;
        this.adminMemo = adminMemo;
        this.processedAt = LocalDateTime.now();
    }
}
