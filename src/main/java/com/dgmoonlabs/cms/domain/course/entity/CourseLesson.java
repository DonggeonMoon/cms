package com.dgmoonlabs.cms.domain.course.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(indexes = @Index(name = "idx_course_lesson_course", columnList = "courseId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CourseLesson extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("과정 ID")
    private Long courseId;

    @Column(nullable = false)
    @Comment("차시 순서 (1부터)")
    private int sequence;

    @Column(nullable = false, length = 200)
    @Comment("차시 제목")
    private String title;

    @Comment("강의 스트리밍 ID (없으면 퀴즈만 있는 차시)")
    private Long streamingId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("퀴즈 문항 (없으면 강의만 있는 차시)")
    private List<QuizQuestion> quiz;

    @Comment("퀴즈 통과 점수(%)")
    private Integer passScore;

    public void update(final int sequence, final String title, final Long streamingId, final List<QuizQuestion> quiz,
                       final Integer passScore) {
        this.sequence = sequence;
        this.title = title;
        this.streamingId = streamingId;
        this.quiz = quiz == null || quiz.isEmpty() ? null : quiz;
        this.passScore = this.quiz == null ? null : passScore;
    }

    public boolean hasQuiz() {
        return quiz != null && !quiz.isEmpty();
    }
}
