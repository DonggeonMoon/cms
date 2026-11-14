package com.dgmoonlabs.cms.domain.course.entity;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Table(indexes = @Index(name = "idx_course_lesson_comment_lesson", columnList = "lessonId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CourseLessonComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("항목(차시) ID")
    private Long lessonId;

    @Comment("부모 질문/댓글 ID (없으면 새 질문)")
    private Long parentId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("댓글 깊이 (0=질문, 1=답글)")
    private int depth;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("작성자 사용자 타입")
    private UserType userType;

    @Comment("작성자 ID")
    private Long userId;

    @Comment("작성자 이름")
    private String authorName;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용")
    private String content;

    public void edit(final String content) {
        this.content = content;
    }
}
