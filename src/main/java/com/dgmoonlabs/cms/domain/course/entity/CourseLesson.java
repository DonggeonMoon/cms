package com.dgmoonlabs.cms.domain.course.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

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
    @Comment("과정(시리즈) ID")
    private Long courseId;

    @Column(nullable = false)
    @Comment("항목 순서 (1부터)")
    private int sequence;

    @Column(nullable = false, length = 200)
    @Comment("항목 제목")
    private String title;

    @Comment("단원명 (화면에서 항목을 묶어 보여줄 때 씀, 없으면 단원 구분 없음)")
    private String unitName;

    @Comment("강의 스트리밍 ID (없으면 퀴즈만 있는 항목)")
    private Long streamingId;

    @Comment("퀴즈 ID (없으면 강의만 있는 항목, 교육과정으로 사용하는 시리즈에서만 설정 가능)")
    private Long quizId;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("필수 항목 여부 (교육과정 수료 진도율 기준과 무관하게 항상 완료해야 함)")
    private boolean mandatory;

    @Comment("건너뛰기 방지 사용 여부(항목 단위 재정의). 없으면 시리즈 전체 설정을 따른다.")
    private Boolean preventSkippingOverride;

    public void update(final int sequence, final String title, final String unitName, final Long streamingId, final Long quizId,
                       final boolean mandatory, final Boolean preventSkippingOverride) {
        this.sequence = sequence;
        this.title = title;
        this.unitName = unitName;
        this.streamingId = streamingId;
        this.quizId = quizId;
        this.mandatory = mandatory;
        this.preventSkippingOverride = preventSkippingOverride;
    }

    public boolean hasQuiz() {
        return quizId != null;
    }

    public boolean effectivePreventSkipping(final Course course) {
        return preventSkippingOverride != null ? preventSkippingOverride : course.effectivePreventSkipping();
    }
}
