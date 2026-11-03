package com.dgmoonlabs.cms.domain.board.entity.qna;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.crypto.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Question extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ColumnDefault("'제목 없음'")
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

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;

    @Comment("작성자 이름")
    private String authorName;

    @Comment("이메일")
    private String email;

    @Comment("연락처")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String phoneNumber;

    @Column(nullable = false)
    @ColumnDefault("false")
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

    @Comment("IP 주소")
    private String ipAddress;

    public void update(final String title, final String contentHtml, final String contentText, final String email,
                       final String phoneNumber, final boolean isSecret) {
        this.title = title;
        this.contentHtml = contentHtml;
        this.contentText = contentText;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.isSecret = isSecret;
    }

    public void increaseHit() {
        this.hit++;
    }

    public boolean isWrittenByGuest() {
        return userType == UserType.GUEST;
    }
}
