package com.dgmoonlabs.cms.domain.admin.site.entity;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
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
public class Site extends BaseEntity {
    @Id
    private Long id;

    @Comment("사이트 이름")
    private String name;

    @Comment("사이트 설명")
    private String description;

    @Comment("사이트 도메인")
    private String domain;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("사이트 종류")
    private MenuType type;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("기본 사이트 여부")
    private boolean isDefault;

    @Comment("사이트 로케일")
    private String locale;
}
