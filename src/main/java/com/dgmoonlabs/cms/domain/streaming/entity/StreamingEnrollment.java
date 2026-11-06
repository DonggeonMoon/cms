package com.dgmoonlabs.cms.domain.streaming.entity;

import com.dgmoonlabs.cms.domain.streaming.constant.EnrollmentStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_streaming_enrollment", columnNames = {"streamingId", "memberId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class StreamingEnrollment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("스트리밍 ID")
    private Long streamingId;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("수강 상태")
    private EnrollmentStatus status = EnrollmentStatus.IN_PROGRESS;

    @Comment("수강 완료 일시")
    private LocalDateTime completedAt;

    public static StreamingEnrollment start(final Long streamingId, final Long memberId) {
        return StreamingEnrollment.builder().streamingId(streamingId).memberId(memberId).build();
    }

    public boolean isCompleted() {
        return status == EnrollmentStatus.COMPLETED;
    }

    public void complete() {
        if (!isCompleted()) {
            this.status = EnrollmentStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }
}
