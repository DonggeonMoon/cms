package com.dgmoonlabs.cms.domain.business.category.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ProductCategory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    @Comment("카테고리 이름")
    private String name;

    @Comment("상위 카테고리 ID (없으면 최상위)")
    private Long parentId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("정렬 순서")
    private int sequence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("상품 속성 정의 (하위 카테고리는 상위 속성을 물려받음)")
    private List<CategoryAttribute> attributes;

    public void update(final String name, final Long parentId, final int sequence, final List<CategoryAttribute> attributes) {
        this.name = name;
        this.parentId = parentId;
        this.sequence = sequence;
        this.attributes = attributes;
    }

    public List<CategoryAttribute> attributesOrEmpty() {
        return attributes == null ? List.of() : attributes;
    }
}
