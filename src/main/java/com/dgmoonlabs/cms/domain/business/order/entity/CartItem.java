package com.dgmoonlabs.cms.domain.business.order.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_cart_item_member", columnList = "memberId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CartItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @Comment("상품 ID")
    private Long productId;

    @Comment("상품 옵션(조합) ID")
    private Long productOptionId;

    @Column(nullable = false)
    @Comment("수량")
    private int quantity;

    @Comment("장바구니 이탈 알림을 보낸 일시 (중복 발송 방지용)")
    private LocalDateTime remindedAt;

    public void changeQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public void markReminded(final LocalDateTime now) {
        this.remindedAt = now;
    }
}
