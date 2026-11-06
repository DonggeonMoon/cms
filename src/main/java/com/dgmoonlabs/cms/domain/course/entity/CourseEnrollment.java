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

    public void complete(final LocalDateTime now) {
        if (status != EnrollmentStatus.COMPLETED) {
            this.status = EnrollmentStatus.COMPLETED;
            this.completedAt = now;
        }
    }
}
