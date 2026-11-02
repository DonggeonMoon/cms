package com.dgmoonlabs.cms.domain.business.order.entity.product;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ProductOption extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("상품 ID")
    private Long productId;

    @Comment("옵션명")
    private String name;

    @Comment("옵션 설명")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    @ColumnDefault("0")
    @Comment("추가 금액")
    private BigDecimal additionalPrice;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;
}
