package com.dgmoonlabs.cms.domain.business.order.entity;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
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

    @Comment("주문 번호")
    private String orderNumber;

    @Comment("주문 시퀀스")
    private String orderSequence;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;

    @Comment("제품 ID")
    private Long productId;

    @Comment("수량")
    private Integer quantity;

    @Comment("할인 ID")
    private Long discountId;

    @Comment("총 가격")
    private BigDecimal totalPrice;

    @ColumnDefault("0")
    @Column(nullable = false)
    @Comment("배송 가능 여부")
    private boolean canDeliver;

    @Comment("우편번호")
    private String postalCode;

    @Comment("주소")
    private String address;

    @Comment("상세주소")
    private String detailAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;
}
