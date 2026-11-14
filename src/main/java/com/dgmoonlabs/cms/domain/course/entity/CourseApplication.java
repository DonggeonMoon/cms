package com.dgmoonlabs.cms.domain.course.entity;

import com.dgmoonlabs.cms.domain.course.constant.CourseApplicationStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * "신청 후 수강"·"승인제" 시리즈/교육과정의 수강 신청 기록. 수강 기록(CourseEnrollment)과는 별도 테이블로 두고,
 * 반려·취소 후에도 다시 신청할 수 있도록 새 행을 쌓는다(과거 신청 이력도 그대로 남는다).
 */
@Entity
@Table(indexes = {
        @Index(name = "idx_course_application_course", columnList = "courseId"),
        @Index(name = "idx_course_application_member", columnList = "memberId")
})
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CourseApplication extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("과정(시리즈) ID")
    private Long courseId;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("신청 상태")
    private CourseApplicationStatus status = CourseApplicationStatus.APPLIED;

    @Comment("승인·반려·취소 처리 일시")
    private LocalDateTime decidedAt;

    public static CourseApplication apply(final Long courseId, final Long memberId) {
        return CourseApplication.builder().courseId(courseId).memberId(memberId).build();
    }

    public boolean isPending() {
        return status == CourseApplicationStatus.APPLIED;
    }

    public void approve(final LocalDateTime now) {
        this.status = CourseApplicationStatus.APPROVED;
        this.decidedAt = now;
    }

    public void reject(final LocalDateTime now) {
        this.status = CourseApplicationStatus.REJECTED;
        this.decidedAt = now;
    }

    public void cancel(final LocalDateTime now) {
        this.status = CourseApplicationStatus.CANCELED;
        this.decidedAt = now;
    }
}
