package com.dgmoonlabs.cms.domain.admin.menu.entity;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Menu {
    @Id
    private Long id;

    @Comment("메뉴 이름")
    private String name;

    @Comment("메뉴 종류")
    private MenuType type;

    @Comment("부모 댓글 ID")
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


}
