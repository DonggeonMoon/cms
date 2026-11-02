package com.dgmoonlabs.cms.domain.business.order.entity;

import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "`order`")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Comment("주문 번호")
    private String orderNumber;

    @Comment("주문 순번")
    private String orderSequence;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;

    @Comment("상품 ID")
    private Long productId;

    @Comment("상품 옵션 ID")
    private Long productOptionId;

    @Comment("주문 당시 상품명")
    private String productName;

    @Comment("수량")
    private Integer quantity;

    @Comment("할인 ID")
    private Long discountId;

    @Column(precision = 15, scale = 2)
    @Comment("총 금액")
    private BigDecimal totalPrice;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("주문 상태")
    private OrderStatus status;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("배송 가능 여부")
    private boolean canDeliver;

    @Comment("수령인")
    private String receiverName;

    @Comment("연락처")
    private String phoneNumber;

    @Comment("우편번호")
    private String postalCode;

    @Comment("주소")
    private String address;

    @Comment("상세 주소")
    private String detailAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    public void markPaid() {
        requireStatus(OrderStatus.PENDING_PAYMENT);
        this.status = OrderStatus.PAID;
    }

    public void moveTo(final OrderStatus next) {
        if (!status.canMoveTo(next)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = next;
    }

    public void cancel() {
        if (!status.isCancelable()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = OrderStatus.CANCELED;
    }

    private void requireStatus(final OrderStatus expected) {
        if (status != expected) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}
