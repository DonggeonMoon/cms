package com.dgmoonlabs.cms.domain.application.entity;

import com.dgmoonlabs.cms.domain.application.constant.SubmissionStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(indexes = @Index(name = "idx_application_submission_form", columnList = "formId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ApplicationSubmission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("신청서 ID")
    private Long formId;

    @Comment("신청서 제목 (신청 당시)")
    private String formTitle;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("입력한 답 (항목 키 → 값)")
    private String answers;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("심사 상태")
    private SubmissionStatus status = SubmissionStatus.RECEIVED;

    @Column(columnDefinition = "text")
    @Comment("신청자에게 보이는 안내")
    private String resultMessage;

    @Column(columnDefinition = "text")
    @Comment("관리자 메모")
    private String adminMemo;

    @Comment("선정 후 결제 주문 ID (입학금·입회비, 결제는 그 주문에서 받음)")
    private Long admissionOrderId;

    public void linkAdmissionOrder(final Long orderId) {
        this.admissionOrderId = orderId;
    }

    public void review(final SubmissionStatus status, final String resultMessage, final String adminMemo) {
        if (status == SubmissionStatus.WITHDRAWN || this.status == SubmissionStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = status;
        this.resultMessage = resultMessage;
        this.adminMemo = adminMemo;
    }

    public void drawResult(final boolean won) {
        this.status = won ? SubmissionStatus.ACCEPTED : SubmissionStatus.REJECTED;
        this.resultMessage = won ? "추첨에 당첨되었습니다." : "아쉽게도 추첨에 당첨되지 않았습니다.";
    }

    public void withdraw() {
        if (status != SubmissionStatus.RECEIVED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = SubmissionStatus.WITHDRAWN;
    }
}
