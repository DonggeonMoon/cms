package com.dgmoonlabs.cms.domain.application.entity;

import com.dgmoonlabs.cms.domain.application.constant.SelectionMethod;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ApplicationForm extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @Comment("제목")
    private String title;

    @Column(columnDefinition = "text")
    @Comment("안내 HTML")
    private String contentHtml;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("입력 항목 정의")
    private String fields;

    @Comment("접수 시작 일시")
    private LocalDateTime startDateTime;

    @Comment("접수 마감 일시")
    private LocalDateTime endDateTime;

    @Comment("최대 접수 건수 (없으면 제한 없음)")
    private Integer maximumSubmissions;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("한 회원이 여러 번 신청할 수 있는지")
    private boolean allowMultiple;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("공개 여부")
    private boolean isEnabled;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'REVIEW'")
    @Enumerated(EnumType.STRING)
    @Comment("선정 방식 (심사 / 추첨)")
    private SelectionMethod selectionMethod = SelectionMethod.REVIEW;

    @Comment("추첨 당첨 인원")
    private Integer winnerCount;

    @Comment("추첨한 일시")
    private LocalDateTime drawnAt;

    @Comment("선정 후 결제할 상품 ID (입학금·입회비 등, 없으면 결제 없이 결과만 안내)")
    private Long admissionFeeProductId;

    @Column(columnDefinition = "text")
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("심사위원 회원 ID 목록 (공모전)")
    private String judgeMemberIds;

    public void update(final String title, final String contentHtml, final String fields, final LocalDateTime startDateTime,
                       final LocalDateTime endDateTime, final Integer maximumSubmissions, final boolean allowMultiple,
                       final boolean isEnabled, final SelectionMethod selectionMethod, final Integer winnerCount,
                       final Long admissionFeeProductId, final String judgeMemberIds) {
        this.title = title;
        this.contentHtml = contentHtml;
        this.fields = fields;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.maximumSubmissions = maximumSubmissions;
        this.allowMultiple = allowMultiple;
        this.isEnabled = isEnabled;
        this.admissionFeeProductId = admissionFeeProductId;
        if (drawnAt == null) {
            this.selectionMethod = selectionMethod;
            this.winnerCount = winnerCount;
            this.judgeMemberIds = judgeMemberIds;
        }
    }

    public void markDrawn(final LocalDateTime now) {
        if (selectionMethod != SelectionMethod.LOTTERY) {
            throw new BusinessException(ErrorCode.NOT_LOTTERY);
        }
        if (drawnAt != null) {
            throw new BusinessException(ErrorCode.ALREADY_DRAWN);
        }
        this.drawnAt = now;
    }

    public void markContestFinalized(final LocalDateTime now) {
        if (selectionMethod != SelectionMethod.CONTEST) {
            throw new BusinessException(ErrorCode.NOT_CONTEST);
        }
        if (drawnAt != null) {
            throw new BusinessException(ErrorCode.ALREADY_FINALIZED);
        }
        this.drawnAt = now;
    }

    public boolean isOpenAt(final LocalDateTime now) {
        return isEnabled && getDeletedAt() == null && drawnAt == null
                && (startDateTime == null || !now.isBefore(startDateTime))
                && (endDateTime == null || !now.isAfter(endDateTime));
    }
}
