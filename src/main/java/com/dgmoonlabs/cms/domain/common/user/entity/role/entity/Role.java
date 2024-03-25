package com.dgmoonlabs.cms.domain.common.user.entity.role.entity;


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
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("역할 코드")
    private String code;

    @Comment("역할 이름")
    private String name;

    @Comment("역할 설명")
    private String description;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("기본 역할 여부")
    private boolean isDefault;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("관리자 여부")
    private boolean isAdmin;
}
