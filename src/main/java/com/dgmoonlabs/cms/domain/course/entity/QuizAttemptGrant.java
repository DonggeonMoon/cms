package com.dgmoonlabs.cms.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

/** 관리자가 특정 항목에서 특정 회원에게 더 응시할 수 있게 추가로 부여한 횟수. 퀴즈의 재응시 허용 횟수에 더해진다. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_quiz_attempt_grant", columnNames = {"lessonId", "memberId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class QuizAttemptGrant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("차시 ID")
    private Long lessonId;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("추가로 부여한 응시 횟수")
    private int extraAttempts;

    public void grant(final int count) {
        this.extraAttempts += count;
    }
}
