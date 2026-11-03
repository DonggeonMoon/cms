package com.dgmoonlabs.cms.domain.archive.entity.comment;

import com.dgmoonlabs.cms.domain.board.constant.CommentStatus;
import com.dgmoonlabs.cms.domain.board.constant.SnsType;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Table(indexes = @Index(name = "idx_archive_comment_article", columnList = "articleId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ArchiveComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("아카이브 글 ID")
    private Long articleId;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;

    @Comment("작성자 이름")
    private String authorName;

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

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
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

    public void edit(final String content) {
        this.content = content;
    }
}
