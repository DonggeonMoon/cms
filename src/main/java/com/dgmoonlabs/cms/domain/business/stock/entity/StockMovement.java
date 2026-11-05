package com.dgmoonlabs.cms.domain.business.stock.entity;

import com.dgmoonlabs.cms.domain.business.stock.constant.StockMovementType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/** 재고 변동 이력. 재고 무제한(availableQuantity=null) 상품은 quantityAfter가 비어 있다. */
@Entity
@Table(indexes = @Index(name = "idx_stock_movement_product", columnList = "productId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class StockMovement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("상품 ID")
    private Long productId;

    @Comment("옵션 ID")
    private Long productOptionId;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("종류 (OUT=출고, IN=입고·복원, ADJUST=수동 조정)")
    private StockMovementType type;

    @Column(nullable = false)
    @Comment("변동 수량 (항상 양수, 방향은 type으로 구분)")
    private int quantity;

    @Comment("관련 주문 ID")
    private Long orderId;

    @Comment("처리 후 재고 (재고 무제한 상품은 비어 있음)")
    private Integer quantityAfter;

    @Column(length = 200)
    @Comment("메모")
    private String note;
}
