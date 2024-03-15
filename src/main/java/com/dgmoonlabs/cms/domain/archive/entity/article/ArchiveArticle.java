package com.dgmoonlabs.cms.domain.archive.entity.article;

import com.dgmoonlabs.cms.domain.archive.constant.ArchiveStatus;
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

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ArchiveArticle extends BaseEntity {
    @Id
    private Long id;

    @Column
    @Comment("아카이브 ID")
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

    @Comment("사용자 타입")
    private UserType userType;

    @Comment("사용자 ID")
    private Long userId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("조회 수")
    private int hit;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("추천/좋아요/하트 수")
    private int likes;

    @Comment("아카이브 상태")
    private ArchiveStatus status;

    @Column(nullable = false)
    @Comment("IP 주소")
    private String ipAddress;
}
