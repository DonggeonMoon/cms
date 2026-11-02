package com.dgmoonlabs.cms.domain.business.discount.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Discount extends BaseEntity {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("상품 ID")
    private Long productId;

    @Comment("할인명")
    private String name;

    @Comment("할인 설명")
    private String description;

    @Column(precision = 5, scale = 2)
    @Comment("할인율 (%)")
    private BigDecimal rate;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("사용 여부")
    private boolean isUsing;

    public void update(final Long productId, final String name, final String description, final BigDecimal rate,
                       final boolean isUsing) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.rate = rate;
        this.isUsing = isUsing;
    }

    public BigDecimal apply(final BigDecimal amount) {
        BigDecimal ratio = HUNDRED.subtract(rate).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return amount.multiply(ratio).setScale(0, RoundingMode.HALF_UP);
    }

    public boolean isApplicableTo(final Long productId) {
        return isUsing && getDeletedAt() == null && (this.productId == null || this.productId.equals(productId));
    }
}
