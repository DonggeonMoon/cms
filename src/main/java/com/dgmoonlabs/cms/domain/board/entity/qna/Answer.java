package com.dgmoonlabs.cms.domain.board.entity.qna;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Answer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_ref_id", nullable = false, unique = true)
    @Comment("질문 ID")
    private Long questionId;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 텍스트")
    private String contentText;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 HTML")
    private String contentHtml;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("사용자 타입")
    private UserType userType;

    @Comment("답변자 ID")
    private Long userId;

    @Comment("답변자 이름")
    private String authorName;

    public void update(final String contentHtml, final String contentText, final Long userId, final String authorName) {
        this.contentHtml = contentHtml;
        this.contentText = contentText;
        this.userId = userId;
        this.authorName = authorName;
    }
}
