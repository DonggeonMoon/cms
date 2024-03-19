package com.dgmoonlabs.cms.domain.government.report.entity.answer;

import com.dgmoonlabs.cms.domain.board.constant.CommentStatus;
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
public class ReportAnswer extends BaseEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    @Comment("신고 ID")
    private String reportId;

    @Comment("부모 댓글 ID")
    private Long parentId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("깊이")
    private int depth;

    @Column(name = "`order`", nullable = false)
    @ColumnDefault("0")
    @Comment("순서")
    private int order;

    @Column(nullable = false)
    @ColumnDefault("'제목 없음'")
    @Comment("제목")
    private String title;

    @Lob
    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("내용 텍스트")
    private String contentText;

    @Lob
    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("내용 HTML")
    private String contentHtml;

    @Comment("댓글 상태")
    @Enumerated(EnumType.STRING)
    private CommentStatus status;
}
