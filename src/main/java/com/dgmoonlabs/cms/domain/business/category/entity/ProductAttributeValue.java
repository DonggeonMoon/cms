package com.dgmoonlabs.cms.domain.business.category.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Table(indexes = {
        @Index(name = "idx_product_attribute_product", columnList = "productId"),
        @Index(name = "idx_product_attribute_text", columnList = "attributeKey, textValue"),
        @Index(name = "idx_product_attribute_number", columnList = "attributeKey, numberValue")
})
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ProductAttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("상품 ID")
    private Long productId;

    @Column(nullable = false, length = 40)
    @Comment("속성 키")
    private String attributeKey;

    @Column(length = 200)
    @Comment("선택형·텍스트 값")
    private String textValue;

    @Column(precision = 15, scale = 4)
    @Comment("숫자 값")
    private BigDecimal numberValue;

    public Object value() {
        return numberValue != null ? numberValue : textValue;
    }
}
