package com.dgmoonlabs.cms.domain.common.grade.entity;

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
public class MemberGrade extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @Comment("등급 이름")
    private String name;

    @Column(length = 255)
    @Comment("등급 설명")
    private String description;

    @Column(name = "grade_rank", nullable = false)
    @Comment("순위 (클수록 높은 등급)")
    private int rank;

    @Column(name = "is_default", nullable = false)
    @ColumnDefault("false")
    @Comment("기본 등급 여부 (등급이 없는 회원에게 적용)")
    private boolean isDefault;

    public void update(final String name, final String description, final int rank) {
        this.name = name;
        this.description = description;
        this.rank = rank;
    }

    public void markDefault(final boolean isDefault) {
        this.isDefault = isDefault;
    }
}
