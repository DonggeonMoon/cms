package com.dgmoonlabs.cms.domain.board.entity.aritcle;

import com.dgmoonlabs.cms.domain.board.constant.BoardType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("소속 사이트 ID")
    private Long siteId;

    @Column(nullable = false)
    @Comment("게시판 이름")
    private String name;

    @Comment("게시판 설명")
    private String description;

    @Builder.Default
    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("게시판 타입")
    private BoardType type = BoardType.NORMAL;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    public void update(final String name, final String description, final BoardType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }
}
