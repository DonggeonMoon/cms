package com.dgmoonlabs.cms.domain.board.entity.comment;

import com.dgmoonlabs.cms.domain.board.constant.CommentStatus;
import com.dgmoonlabs.cms.domain.board.constant.SnsType;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Table(indexes = @Index(name = "idx_board_comment_article", columnList = "articleId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BoardComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("게시글 ID")
    private Long articleId;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;

    @Comment("작성자 이름")
    private String authorName;

    @Comment("비회원 비밀번호 (해시)")
    private String password;

    @Comment("부모 댓글 ID")
    private Long parentId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("댓글 깊이")
    private int depth;

    @Column(name = "`order`", nullable = false)
    @ColumnDefault("0")
    @Comment("댓글 순서")
    private int order;

    @Comment("제목")
    private String title;

    @Column(columnDefinition = "text")
    @Comment("내용")
    private String content;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("SNS 타입")
    private SnsType snsType;

    @Comment("SNS 아이디")
    private String snsUsername;

    @Comment("SNS 사용자 URL")
    private String snsUserUrl;

    @Comment("프로필 이미지 파일 ID")
    private Long profileImageFileId;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("댓글 상태")
    private CommentStatus status;

    @Comment("작성자 IP 주소")
    private String ipAddress;

    @Comment("조회 수")
    private int hit;

    public boolean isWrittenByGuest() {
        return userType == UserType.GUEST;
    }

    public void edit(final String content) {
        this.content = content;
    }

    public void moderate() {
        this.status = CommentStatus.MODERATED;
    }
}
