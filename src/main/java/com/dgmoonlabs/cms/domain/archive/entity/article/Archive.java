package com.dgmoonlabs.cms.domain.archive.entity.article;

import com.dgmoonlabs.cms.domain.board.constant.BoardType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Archive extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Comment("아카이브 이름")
    private String name;

    @Comment("아카이브 설명")
    private String description;

    @Builder.Default
    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("아카이브 타입")
    private BoardType type = BoardType.NORMAL;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("댓글 기능 활성화 여부")
    private boolean supportsComment;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("추천 기능 활성화 여부")
    private boolean supportsLike;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("10")
    @Comment("페이지 당 글 수")
    private int pageSize = 10;

    @Comment("열람 가능 등급 ID (없으면 누구나)")
    private Long accessGradeId;

    public void changeAccessGrade(final Long accessGradeId) {
        this.accessGradeId = accessGradeId;
    }

    public void update(final String name, final String description, final BoardType type, final boolean supportsComment,
                       final boolean supportsLike, final int pageSize) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.supportsComment = supportsComment;
        this.supportsLike = supportsLike;
        this.pageSize = pageSize;
    }
}
