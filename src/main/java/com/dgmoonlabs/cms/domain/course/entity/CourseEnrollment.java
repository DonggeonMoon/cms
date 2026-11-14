package com.dgmoonlabs.cms.domain.course.entity;

import com.dgmoonlabs.cms.domain.streaming.constant.EnrollmentStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_course_enrollment", columnNames = {"courseId", "memberId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CourseEnrollment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("과정 ID")
    private Long courseId;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("수강 상태")
    private EnrollmentStatus status = EnrollmentStatus.IN_PROGRESS;

    @Comment("과정 완료 일시")
    private LocalDateTime completedAt;

    @Comment("관리자가 개별로 연장한 수강 마감 일시 (없으면 연장 없음)")
    private LocalDateTime accessExtendedUntil;

    @Comment("결제로 수강하게 된 경우 그 주문 ID (없으면 결제 없이 수강)")
    private Long orderId;

    @Comment("환불로 수강 권한을 회수한 일시 (없으면 유효)")
    private LocalDateTime revokedAt;

    public void complete(final LocalDateTime now) {
        if (status != EnrollmentStatus.COMPLETED) {
            this.status = EnrollmentStatus.COMPLETED;
            this.completedAt = now;
        }
    }

    public void extendAccess(final LocalDateTime until) {
        this.accessExtendedUntil = until;
    }

    public void linkOrder(final Long orderId) {
        this.orderId = orderId;
    }

    public void revoke(final LocalDateTime now) {
        if (revokedAt == null) {
            revokedAt = now;
        }
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
