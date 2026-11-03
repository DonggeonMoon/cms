package com.dgmoonlabs.cms.domain.media.entity;

import com.dgmoonlabs.cms.domain.media.constant.EnrollmentStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_media_enrollment", columnNames = {"mediaId", "memberId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MediaEnrollment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("스트리밍 ID")
    private Long mediaId;

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

    public static MediaEnrollment start(final Long mediaId, final Long memberId) {
        return MediaEnrollment.builder().mediaId(mediaId).memberId(memberId).build();
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
