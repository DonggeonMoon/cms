package com.dgmoonlabs.cms.domain.common.user.entity.group.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "`group`")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Group extends BaseEntity {
    @Id
    private Long id;

    @Comment("그룹 이름")
    private String name;

    @Comment("그룹 설명")
    private String description;

    @Comment("부모 그룹 ID")
    private Long parentId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("그룹 깊이")
    private int depth;

    @Column(name = "`order`", nullable = false)
    @ColumnDefault("0")
    @Comment("그룹 순서")
    private int order;
}
