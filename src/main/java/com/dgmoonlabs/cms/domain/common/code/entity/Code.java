package com.dgmoonlabs.cms.domain.common.code.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Code extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("부모 코드 ID")
    private Long parentId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("코드 깊이")
    private int depth;

    @Column(name = "`order`", nullable = false)
    @ColumnDefault("0")
    @Comment("코드 순서")
    private int order;

    @Comment("코드 이름")
    private String name;

    @Comment("코드 영어 이름")
    private String englishName;

    @Comment("코드 설명")
    private String description;

    @Column(nullable = false)
    @ColumnDefault("1")
    @Comment("사용 여부")
    private boolean isUsing;
}
