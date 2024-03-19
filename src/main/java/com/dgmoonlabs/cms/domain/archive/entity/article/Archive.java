package com.dgmoonlabs.cms.domain.archive.entity.article;

import com.dgmoonlabs.cms.domain.board.constant.BoardType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Archive extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Comment("아카이브 이름")
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("아카이브 타입")
    private BoardType type = BoardType.NORMAL;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("댓글 기능 활성화 여부")
    private boolean supportsComment;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("페이지 당 글 수")
    private int pageSize = 10;
}
