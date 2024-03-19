package com.dgmoonlabs.cms.domain.admin.site.entity;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
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

    @Comment("사이트 종류")
    @Enumerated(EnumType.STRING)
    private MenuType type;

    @Comment("기본 사이트 여부")
    private boolean isMain;

    @Comment("사이트 로케일")
    private String locale;
}
