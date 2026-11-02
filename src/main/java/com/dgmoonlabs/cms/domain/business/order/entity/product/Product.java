package com.dgmoonlabs.cms.domain.business.order.entity.product;

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
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("상품명")
    private String name;

    @Comment("상품 설명")
    private String description;

    @Comment("대표 이미지 파일 ID")
    private Long imageFileId;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("가격")
    private BigDecimal price;

    @Comment("재고 수량 (null: 무제한)")
    private Integer availableQuantity;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 텍스트")
    private String contentText;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 HTML")
    private String contentHtml;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("판매 여부")
    private boolean isEnabled;

    public void update(final String name, final String description, final Long imageFileId, final BigDecimal price,
                       final Integer availableQuantity, final String contentHtml, final String contentText,
                       final boolean isEnabled) {
        this.name = name;
        this.description = description;
        this.imageFileId = imageFileId;
        this.price = price;
        this.availableQuantity = availableQuantity;
        this.contentHtml = contentHtml;
        this.contentText = contentText;
        this.isEnabled = isEnabled;
    }

    public void decreaseStock(final int quantity) {
        if (availableQuantity == null) {
            return;
        }
        if (availableQuantity < quantity) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
        availableQuantity -= quantity;
    }

    public void increaseStock(final int quantity) {
        if (availableQuantity != null) {
            availableQuantity += quantity;
        }
    }

    public boolean isOnSale() {
        return isEnabled && getDeletedAt() == null;
    }
}
