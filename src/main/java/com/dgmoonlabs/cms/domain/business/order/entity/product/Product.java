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
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("이름")
    private String name;

    @Comment("설명")
    private String description;

    @Comment("이미지 ID")
    private Long imageFileId;

    @Comment("가격")
    private BigDecimal price;

    @Comment("주문/구매 가능 수량")
    private Integer availableQuantity;

    @Lob
    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("내용 텍스트")
    private String contentText;

    @Lob
    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("내용 HTML")
    private String contentHtml;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    @Column(nullable = false)
    @ColumnDefault("1")
    @Comment("주문 가능 여부")
    private boolean isEnabled;
}
