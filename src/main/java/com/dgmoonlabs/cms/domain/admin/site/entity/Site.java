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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("사이트 이름")
    private String name;

    @Comment("사이트 설명")
    private String description;

    @Comment("사이트 도메인")
    private String domain;

    @Comment("사이트 테마")
    private String theme;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("메뉴 종류")
    private MenuType type;

    @Comment("사이트 로케일")
    private String locale;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("기본 사이트 여부")
    private boolean isDefault;

    public void update(String name, String description, String domain, String theme, MenuType type, String locale, boolean isDefault) {
        this.name = name;
        this.description = description;
        this.domain = domain;
        this.theme = theme;
        this.type = type;
        this.locale = locale;
        this.isDefault = isDefault;
    }
}
