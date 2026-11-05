package com.dgmoonlabs.cms.domain.board.entity.aritcle;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BoardArticle extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("게시판 ID")
    private Long boardId;

    @Column
    @Comment("제목")
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 텍스트")
    private String contentText;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 HTML")
    private String contentHtml;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    @ColumnDefault("false")
    @Column(nullable = false)
    @Comment("공지 여부")
    private boolean isNotice;

    @Comment("공지 시작 일시")
    private LocalDateTime noticeStartDateTime;

    @Comment("공지 종료 일시")
    private LocalDateTime noticeEndDateTime;

    @ColumnDefault("false")
    @Column(nullable = false)
    @Comment("비공개 여부")
    private boolean isHidden;

    @ColumnDefault("false")
    @Column(nullable = false)
    @Comment("유료 글 여부 (결제한 회원만 본문을 본다)")
    private boolean paid;

    @Column(precision = 15, scale = 2)
    @Comment("판매 가격")
    private BigDecimal price;

    @Comment("판매용으로 자동 관리하는 상품 ID")
    private Long productId;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;

    @Comment("작성자 이름")
    private String authorName;

    @ColumnDefault("false")
    @Column(nullable = false)
    @Comment("비밀글 여부")
    private boolean isSecret;

    @Comment("비회원 비밀번호 (해시)")
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

    public void update(final String title, final String contentHtml, final String contentText, final boolean isSecret) {
        this.title = title;
        this.contentHtml = contentHtml;
        this.contentText = contentText;
        this.isSecret = isSecret;
    }

    public void updateAdminFlags(final boolean isNotice, final LocalDateTime noticeStartDateTime,
                                 final LocalDateTime noticeEndDateTime, final boolean isHidden) {
        this.isNotice = isNotice;
        this.noticeStartDateTime = noticeStartDateTime;
        this.noticeEndDateTime = noticeEndDateTime;
        this.isHidden = isHidden;
    }

    public void changeSale(final boolean paid, final BigDecimal price) {
        this.paid = paid;
        this.price = paid ? price : null;
    }

    public void linkProduct(final Long productId) {
        this.productId = productId;
    }

    public void increaseHit() {
        this.hit++;
    }

    public void increaseLikes() {
        this.likes++;
    }

    public boolean isWrittenByGuest() {
        return userType == UserType.GUEST;
    }
}
