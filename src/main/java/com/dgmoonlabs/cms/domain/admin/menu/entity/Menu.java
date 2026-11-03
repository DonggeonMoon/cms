package com.dgmoonlabs.cms.domain.admin.menu.entity;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.menu.dto.MenuSaveRequest;
import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
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
public class Menu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("메뉴 이름")
    private String name;

    @Comment("메뉴 종류")
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private MenuType type;

    @Comment("부모 메뉴 ID")
    private Long parentId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("깊이")
    private int depth;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("순서")
    private int sequence;

    @Comment("링크")
    private String link;

    @Comment("연결 대상 ID (CONTENT: 컨텐츠 ID, BOARD: 게시판 ID, ARCHIVE: 아카이브 ID)")
    private Long targetId;

    @Comment("기능 메뉴가 가리키는 기능 (FEATURE 메뉴만)")
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)")
    private SiteFeature feature;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("사용 여부")
    @Builder.Default
    private boolean isUsing = true;

    public boolean isFeatureMenu() {
        return type == MenuType.FEATURE;
    }

    public void moveTo(final int sequence) {
        this.sequence = sequence;
    }

    public void update(final MenuSaveRequest request) {
        this.name = request.name();
        this.sequence = request.sequence();
        this.isUsing = request.isUsingOrDefault();
        this.type = request.type();
        this.feature = request.type() == MenuType.FEATURE ? request.feature() : null;
        this.link = request.type() == MenuType.LINK ? request.link() : null;
        this.targetId = request.type() == MenuType.FEATURE || request.type() == MenuType.LINK ? null : request.targetId();
    }
}
