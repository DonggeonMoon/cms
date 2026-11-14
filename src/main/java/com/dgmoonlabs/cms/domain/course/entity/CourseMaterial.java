package com.dgmoonlabs.cms.domain.course.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * 시리즈/교육과정의 학습자료. lessonId가 없으면 과정 전체 자료, 있으면 그 항목 전용 자료다.
 * 수강 기록·시리즈 테이블과 분리된 단순 첨부 목록이라 BaseEntity 없이 저장 시 통째로 지우고 다시 쌓는다.
 */
@Entity
@Table(indexes = {
        @Index(name = "idx_course_material_course", columnList = "courseId"),
        @Index(name = "idx_course_material_lesson", columnList = "lessonId")
})
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CourseMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("과정(시리즈) ID")
    private Long courseId;

    @Comment("항목 ID (없으면 과정 전체 자료)")
    private Long lessonId;

    @Column(nullable = false)
    @Comment("파일 ID")
    private Long fileId;

    @Column(nullable = false)
    @Comment("첨부 순서")
    private int sequence;
}
