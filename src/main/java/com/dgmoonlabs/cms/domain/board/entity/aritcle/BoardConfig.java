package com.dgmoonlabs.cms.domain.board.entity.aritcle;

import com.dgmoonlabs.cms.domain.board.dto.BoardSaveRequest;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "BOARD_CONFIG")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BoardConfig extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Comment("게시판 ID")
    private Long boardId;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("회원 목록 조회 권한")
    private boolean isMemberAccessible;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("회원 글 읽기 권한")
    private boolean isMemberReadable;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("회원 글 쓰기 권한")
    private boolean isMemberWritable;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("비회원 목록 조회 권한")
    private boolean isGuestAccessible;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("비회원 글 읽기 권한")
    private boolean isGuestReadable;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("비회원 글 쓰기 권한")
    private boolean isGuestWritable;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("공지 기능 활성화 여부")
    private boolean supportsNotice;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("비밀글 기능 활성화 여부")
    private boolean supportsSecret;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("댓글 기능 활성화 여부")
    private boolean supportsComment;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("추천/좋아요/하트 기능 활성화 여부")
    private boolean supportsLike;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("첨부 파일 기능 활성화 여부")
    private boolean supportsAttachment;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("5")
    @Comment("최대 첨부 파일 수")
    private int maximumFileCount = 5;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("10")
    @Comment("페이지 당 글 수")
    private int pageSize = 10;

    public static BoardConfig of(final Long boardId, final BoardSaveRequest request) {
        BoardConfig config = BoardConfig.builder().boardId(boardId).build();
        config.update(request);
        return config;
    }

    public void update(final BoardSaveRequest request) {
        this.isMemberAccessible = request.isMemberAccessible();
        this.isMemberReadable = request.isMemberReadable();
        this.isMemberWritable = request.isMemberWritable();
        this.isGuestAccessible = request.isGuestAccessible();
        this.isGuestReadable = request.isGuestReadable();
        this.isGuestWritable = request.isGuestWritable();
        this.supportsNotice = request.supportsNotice();
        this.supportsSecret = request.supportsSecret();
        this.supportsComment = request.supportsComment();
        this.supportsLike = request.supportsLike();
        this.supportsAttachment = request.supportsAttachment();
        this.maximumFileCount = request.maximumFileCount();
        this.pageSize = request.pageSize();
    }
}
