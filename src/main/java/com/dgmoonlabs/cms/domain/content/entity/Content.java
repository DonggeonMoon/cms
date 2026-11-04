package com.dgmoonlabs.cms.domain.content.entity;

import com.dgmoonlabs.cms.domain.content.constant.ContentStatus;
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
public class Content extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("컨텐츠 상태")
    private ContentStatus status;

    @Column(nullable = false)
    @ColumnDefault("1")
    @Comment("컨텐츠 버전")
    private int version;

    @Comment("마지막 작업자")
    private String lastWorker;

    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("메모")
    private String memo;

    @Comment("열람 가능 등급 ID (없으면 누구나)")
    private Long accessGradeId;

    public void changeAccessGrade(final Long accessGradeId) {
        this.accessGradeId = accessGradeId;
    }

    public void update(final String title, final String contentHtml, final String contentText, final ContentStatus status,
                       final String memo, final String worker) {
        this.title = title;
        this.contentHtml = contentHtml;
        this.contentText = contentText;
        this.status = status;
        this.memo = memo;
        this.lastWorker = worker;
        this.version++;
    }
}
