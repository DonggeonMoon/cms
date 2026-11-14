package com.dgmoonlabs.cms.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 한 번의 퀴즈 응시(시작~제출) 동안만 사는 임시 상태. 문제·보기를 섞어서 보여준 순서를 기억해 두어야
 * 제출된 답을 원래 문항·보기로 되돌려 채점할 수 있다. 제출하면 지운다.
 */
@Entity
@Table(indexes = @Index(name = "idx_quiz_session_lesson_member", columnList = "lessonId, memberId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class QuizSession {
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
    @Comment("시작 일시")
    private LocalDateTime startedAt;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("보여준 문항 순서 (원래 퀴즈 문항 순번 목록)")
    private List<Integer> questionOrder;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("문항별로 보여준 보기 순서 (원래 보기 순번 목록의 목록)")
    private List<List<Integer>> optionOrders;
}
