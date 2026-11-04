package com.dgmoonlabs.cms.domain.content.entity;

import com.dgmoonlabs.cms.domain.content.constant.ContentStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

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

    @Comment("검토를 요청한 관리자")
    private String reviewRequestedBy;

    @Comment("검토 요청 일시")
    private LocalDateTime reviewRequestedAt;

    @Comment("승인·반려한 관리자")
    private String reviewedBy;

    @Column(length = 1000)
    @Comment("반려 사유 등 검토 의견")
    private String reviewComment;

    @Comment("예약 발행 일시")
    private LocalDateTime publishAt;

    @Comment("발행 일시")
    private LocalDateTime publishedAt;

    public void changeAccessGrade(final Long accessGradeId) {
        this.accessGradeId = accessGradeId;
    }

    /** 검토 중이거나 발행을 기다리는 글을 고치면 검토받은 내용과 달라지므로 임시 저장으로 되돌린다. */
    public void update(final String title, final String contentHtml, final String contentText, final ContentStatus status,
                       final String memo, final String worker) {
        this.title = title;
        this.contentHtml = contentHtml;
        this.contentText = contentText;
        if (status == ContentStatus.PUBLISHED && this.status != ContentStatus.PUBLISHED) {
            publishNow(worker, LocalDateTime.now());
        } else if (status == ContentStatus.DRAFT || this.status == ContentStatus.IN_REVIEW || this.status == ContentStatus.SCHEDULED) {
            this.status = ContentStatus.DRAFT;
            this.publishAt = null;
        }
        this.memo = memo;
        this.lastWorker = worker;
        this.version++;
    }

    public void requestReview(final String worker, final LocalDateTime publishAt, final LocalDateTime now) {
        if (status != ContentStatus.DRAFT && status != ContentStatus.REJECTED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = ContentStatus.IN_REVIEW;
        this.reviewRequestedBy = worker;
        this.reviewRequestedAt = now;
        this.publishAt = publishAt;
        this.reviewedBy = null;
        this.reviewComment = null;
    }

    public void approve(final String reviewer, final LocalDateTime now) {
        requireInReview();
        this.reviewedBy = reviewer;
        if (publishAt != null && publishAt.isAfter(now)) {
            this.status = ContentStatus.SCHEDULED;
        } else {
            publishNow(reviewer, now);
        }
    }

    public void reject(final String reviewer, final String comment) {
        requireInReview();
        this.status = ContentStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewComment = comment;
        this.publishAt = null;
    }

    public void unpublish() {
        if (status != ContentStatus.PUBLISHED && status != ContentStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = ContentStatus.DRAFT;
        this.publishAt = null;
    }

    public void publishIfDue(final LocalDateTime now) {
        if (status == ContentStatus.SCHEDULED && publishAt != null && !publishAt.isAfter(now)) {
            this.status = ContentStatus.PUBLISHED;
            this.publishedAt = publishAt;
        }
    }

    private void publishNow(final String worker, final LocalDateTime now) {
        this.status = ContentStatus.PUBLISHED;
        this.publishedAt = now;
        this.publishAt = null;
        if (reviewedBy == null) {
            this.reviewedBy = worker;
        }
    }

    private void requireInReview() {
        if (status != ContentStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}