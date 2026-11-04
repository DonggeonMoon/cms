package com.dgmoonlabs.cms.domain.course.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(indexes = @Index(name = "idx_quiz_attempt_lesson_member", columnList = "lessonId, memberId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class QuizAttempt extends BaseEntity {
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
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("고른 답 (문항 순서대로 선택지 순번)")
    private List<Integer> answers;

    @Column(nullable = false)
    @Comment("점수(%)")
    private int score;

    @Column(nullable = false)
    @Comment("통과 여부")
    private boolean passed;
}
