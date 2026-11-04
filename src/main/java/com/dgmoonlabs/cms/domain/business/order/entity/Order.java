package com.dgmoonlabs.cms.domain.business.order.entity;

import com.dgmoonlabs.cms.global.crypto.EncryptedStringConverter;
import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
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
import java.time.LocalDateTime;

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

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'PURCHASE'")
    @Enumerated(EnumType.STRING)
    @Comment("판매 방식 (주문 당시)")
    private ProductSaleType saleType = ProductSaleType.PURCHASE;

    @Comment("정기 결제로 만들어진 주문의 구독 ID")
    private Long subscriptionId;

    @Column(columnDefinition = "text")
    @Comment("견적 요청 내용")
    private String requestMessage;

    @Column(columnDefinition = "text")
    @Comment("견적 회신 내용")
    private String quoteMessage;

    @Comment("견적 회신 일시")
    private LocalDateTime quotedAt;

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

    @Column(precision = 15, scale = 2)
    @Comment("상품 금액 합계 (항목이 있는 주문)")
    private BigDecimal itemsAmount;

    @Comment("사용한 쿠폰(발급) ID")
    private Long couponIssueId;

    @Column(precision = 15, scale = 2)
    @Comment("쿠폰 할인액")
    private BigDecimal couponDiscount;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("사용 포인트")
    private long pointsUsed = 0;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("적립 포인트")
    private long pointsEarned = 0;

    @Column(precision = 15, scale = 2)
    @Comment("배송비")
    private BigDecimal shippingFee;

    @Column(precision = 15, scale = 2)
    @Comment("부분 취소로 환불한 누적 금액")
    private BigDecimal refundedAmount;

    @Column(length = 50)
    @Comment("택배사")
    private String courier;

    @Column(length = 50)
    @Comment("송장 번호")
    private String trackingNumber;

    @Comment("발송 일시")
    private LocalDateTime shippedAt;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("주문 상태")
    private OrderStatus status;

    @Column(length = 40)
    @Comment("세부 상태 코드 (기본 상태 아래에 관리자가 추가한 상태)")
    private String customStatus;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("배송 가능 여부")
    private boolean canDeliver;

    @Comment("수령인")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String receiverName;

    @Comment("연락처")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String phoneNumber;

    @Comment("우편번호")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String postalCode;

    @Comment("주소")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String address;

    @Comment("상세 주소")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String detailAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    public void quote(final BigDecimal amount, final String message) {
        this.customStatus = null;
        if (status != OrderStatus.QUOTE_REQUESTED && status != OrderStatus.QUOTED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.totalPrice = amount;
        this.quoteMessage = message;
        this.quotedAt = LocalDateTime.now();
        this.status = OrderStatus.QUOTED;
    }

    public void linkSubscription(final Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void acceptQuote() {
        this.customStatus = null;
        requireStatus(OrderStatus.QUOTED);
        this.status = OrderStatus.PENDING_PAYMENT;
    }

    public boolean holdsStock() {
        return saleType != ProductSaleType.DONATION && (status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.PAID);
    }

    public void markPaid() {
        this.customStatus = null;
        requireStatus(OrderStatus.PENDING_PAYMENT);
        this.status = OrderStatus.PAID;
    }

    public void moveTo(final OrderStatus next) {
        this.customStatus = null;
        if (!status.canMoveTo(next)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = next;
    }

    public void cancel() {
        this.customStatus = null;
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

    public void repriceBeforePayment(final BigDecimal itemsAmount, final BigDecimal shippingFee,
                                     final BigDecimal couponDiscount, final long pointsUsed) {
        requireStatus(OrderStatus.PENDING_PAYMENT);
        this.itemsAmount = itemsAmount;
        this.shippingFee = shippingFee;
        this.couponDiscount = couponDiscount;
        this.pointsUsed = pointsUsed;
        this.totalPrice = itemsAmount.subtract(couponDiscount).subtract(BigDecimal.valueOf(pointsUsed)).add(shippingFee);
    }

    public void recordEarned(final long pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public void recordRefund(final BigDecimal amount, final BigDecimal shippingFee) {
        this.refundedAmount = (refundedAmount == null ? BigDecimal.ZERO : refundedAmount).add(amount);
        this.shippingFee = shippingFee;
    }

    public boolean isPartiallyCancelable() {
        return status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.PAID;
    }

    public void ship(final String courier, final String trackingNumber) {
        this.customStatus = null;
        if (status != OrderStatus.PAID && status != OrderStatus.SHIPPING) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.courier = courier;
        this.trackingNumber = trackingNumber;
        if (status == OrderStatus.PAID) {
            this.status = OrderStatus.SHIPPING;
            this.shippedAt = LocalDateTime.now();
        }
    }

    public BigDecimal paidRemaining() {
        BigDecimal total = totalPrice == null ? BigDecimal.ZERO : totalPrice;
        return refundedAmount == null ? total : total.subtract(refundedAmount);
    }

    public void applyCustomStatus(final String code) {
        this.customStatus = code;
    }
}
