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
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ArchiveComment extends BaseEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;

    @Comment("부모 댓글 ID")
    private Long parentId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("댓글 깊이")
    private int depth;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("댓글 순서")
    private int order;

    @Comment("제목")
    private String title;

    @Lob
    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("내용")
    private String content;

    @Enumerated(EnumType.STRING)
    @Comment("SNS 타입")
    private SnsType snsType;

    @Comment("SNS 아이디")
    private String snsUsername;

    @Comment("SNS 사용자 URL")
    private String snsUserUrl;

    @Comment("프로필 이미지 파일 ID")
    private Long profileImageFileId;

    @Comment("댓글 상태")
    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    @Comment("작성자 IP 주소")
    private String ipAddress;

    @Comment("조회 수")
    private int hit;
}
