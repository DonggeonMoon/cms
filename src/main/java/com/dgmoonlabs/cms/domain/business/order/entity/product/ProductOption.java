package com.dgmoonlabs.cms.domain.business.order.entity.product;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;

import java.math.BigDecimal;
import java.util.List;

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
    @Comment("옵션 조합 값 (옵션 그룹 순서대로)")
    private List<String> optionValues;

    @Comment("조합별 재고 (null: 상품 재고만 따름)")
    private Integer stock;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("판매 여부")
    private boolean isEnabled = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    public void update(final String name, final String description, final BigDecimal additionalPrice,
                       final List<String> optionValues, final Integer stock, final boolean isEnabled) {
        this.name = name;
        this.description = description;
        this.additionalPrice = additionalPrice;
        this.optionValues = optionValues;
        this.stock = stock;
        this.isEnabled = isEnabled;
    }

    public void decreaseStock(final int quantity) {
        if (stock == null) {
            return;
        }
        if (stock < quantity) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
        stock -= quantity;
    }

    public void increaseStock(final int quantity) {
        if (stock != null) {
            stock += quantity;
        }
    }
}
