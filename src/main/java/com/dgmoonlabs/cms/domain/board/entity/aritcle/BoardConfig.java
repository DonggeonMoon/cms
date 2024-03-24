package com.dgmoonlabs.cms.domain.board.entity.aritcle;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private Long id;

    @Comment("권한")
    private String authority;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("회원 목록 조회 권한")
    private boolean isMemberAccessible;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("회원 글 읽기 권한")
    private boolean isMemberReadable;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("회원 글 쓰기 권한")
    private boolean isMemberWritable;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("비회원 목록 조회 권한")
    private boolean isGuestAccessible;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("비회원 글 읽기 권한")
    private boolean isGuestReadable;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("비회원 글 쓰기 권한")
    private boolean isGuestWritable;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("공지 기능 활성화 여부")
    private boolean supportsNotice;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("비밀글 기능 활성화 여부")
    private boolean supportsSecret;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("댓글 기능 활성화 여부")
    private boolean supportsComment;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("추천/좋아요/하트 기능 활성화 여부")
    private boolean supportsLike;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("썸네일 활성화 여부")
    private boolean supportThumbnail;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("썸네일 크롭 기능 활성화 여부")
    private boolean supportsThumbnailCrop;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("썸네일 가로 길이")
    private boolean thumbnailWidth;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("썸네일 세로 길이")
    private boolean thumbnailHeight;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("최대 첨부 파일 수")
    private int maximumFileCount = 5;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("최대 파일 크기")
    private int maximumFileSize = 10;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("페이지 당 글 수")
    private int pageSize = 10;
}
