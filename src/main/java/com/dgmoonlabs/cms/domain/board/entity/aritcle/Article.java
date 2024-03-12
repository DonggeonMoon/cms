package com.dgmoonlabs.cms.domain.board.entity.aritcle;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Article extends BaseEntity {
    @Id
    private Long id;

    @Column
    private Long boardId;

    @Column
    private String title;

    @Lob
    private String contentText;

    @Lob
    private String contentHtml;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String fields;

    @ColumnDefault("0")
    @Column(nullable = false)
    private boolean isNotice;

    private LocalDateTime noticeStartDateTime;

    private LocalDateTime noticeEndDateTime;

    @ColumnDefault("0")
    @Column(nullable = false)
    private boolean isHidden;

    private UserType userType;

    private Long userId;

    @ColumnDefault("0")
    @Column(nullable = false)
    private boolean isSecret;

    private String password;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int hit;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int likes;

    @Column(nullable = false)
    private String ipAddress;
}
