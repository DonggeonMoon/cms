package com.dgmoonlabs.cms.domain.course.entity;

import com.dgmoonlabs.cms.domain.course.constant.RevealPolicy;
import com.dgmoonlabs.cms.domain.course.constant.ScorePolicy;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

/** 시리즈/교육과정 항목으로 참조하는 독립 퀴즈. 여러 항목에서 같은 퀴즈를 재사용할 수 있다. */
@Entity
@Table(indexes = @Index(name = "idx_quiz_site", columnList = "siteId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Quiz extends BaseEntity {
    public static final int DEFAULT_PASS_SCORE = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("소속 사이트 ID")
    private Long siteId;

    @Column(nullable = false, length = 200)
    @Comment("퀴즈 이름")
    private String title;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("문항")
    private List<QuizQuestion> questions;

    @Column(nullable = false)
    @ColumnDefault("60")
    @Comment("통과 점수(%)")
    private int passScore;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("사용 여부")
    private boolean isUsing;

    @Comment("재응시 허용 횟수 (없으면 무제한)")
    private Integer retakeLimit;

    @Comment("제한 시간(분) (없으면 제한 없음)")
    private Integer timeLimitMinutes;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("문제 순서 섞기")
    private boolean shuffleQuestions;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("보기 순서 섞기")
    private boolean shuffleOptions;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'IMMEDIATELY'")
    @Enumerated(EnumType.STRING)
    @Comment("정답·해설 공개 시점 - IMMEDIATELY 제출 직후, AFTER_PASS 통과 후, NEVER 비공개")
    private RevealPolicy revealPolicy = RevealPolicy.IMMEDIATELY;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'HIGHEST'")
    @Enumerated(EnumType.STRING)
    @Comment("여러 번 응시했을 때 인정할 점수 - HIGHEST 최고 점수, LATEST 최종 점수")
    private ScorePolicy scorePolicy = ScorePolicy.HIGHEST;

    public void update(final String title, final List<QuizQuestion> questions, final int passScore, final boolean isUsing,
                       final Integer retakeLimit, final Integer timeLimitMinutes, final boolean shuffleQuestions,
                       final boolean shuffleOptions, final RevealPolicy revealPolicy, final ScorePolicy scorePolicy) {
        this.title = title;
        this.questions = questions;
        this.passScore = passScore;
        this.isUsing = isUsing;
        this.retakeLimit = retakeLimit;
        this.timeLimitMinutes = timeLimitMinutes;
        this.shuffleQuestions = shuffleQuestions;
        this.shuffleOptions = shuffleOptions;
        this.revealPolicy = revealPolicy;
        this.scorePolicy = scorePolicy;
    }
}
