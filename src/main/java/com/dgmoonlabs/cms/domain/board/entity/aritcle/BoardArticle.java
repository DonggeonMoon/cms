package com.dgmoonlabs.cms.domain.board.entity.aritcle;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BoardArticle extends BaseEntity {
    @Id
    private Long id;

    @Column
    @Comment("게시판 ID")
    private Long boardId;

    @Column
    @Comment("제목")
    private String title;

    @Lob
    @Column(nullable = false)
    @ColumnDefault("")
    @Comment("내용 텍스트")
    private String contentText;

    @Lob
    @Column(nullable = false)
    @ColumnDefault("")
    @Comment("내용 HTML")
    private String contentHtml;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    @ColumnDefault("0")
    @Column(nullable = false)
    @Comment("공지 여부")
    private boolean isNotice;

    @Comment("공지 시작 일시")
    private LocalDateTime noticeStartDateTime;

    @Comment("공지 종료 일시")
    private LocalDateTime noticeEndDateTime;

    @ColumnDefault("0")
    @Column(nullable = false)
    @Comment("비공개 여부")
    private boolean isHidden;

    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;

    @ColumnDefault("0")
    @Column(nullable = false)
    @Comment("비밀글 여부")
    private boolean isSecret;

    @Comment("비밀번호")
    private String password;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("조회 수")
    private int hit;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("추천/좋아요/하트 수")
    private int likes;

    @Column(nullable = false)
    @Comment("IP 주소")
    private String ipAddress;
}
