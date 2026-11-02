package com.dgmoonlabs.cms.domain.government.report.entity.answer;

import com.dgmoonlabs.cms.domain.board.constant.CommentStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Table(indexes = @Index(name = "idx_report_answer_report", columnList = "report_ref_id"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ReportAnswer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_ref_id", nullable = false)
    @Comment("신고 ID")
    private Long reportId;

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

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 텍스트")
    private String contentText;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 HTML")
    private String contentHtml;

    @Comment("답변자 이름")
    private String authorName;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("댓글 상태")
    private CommentStatus status;
}
