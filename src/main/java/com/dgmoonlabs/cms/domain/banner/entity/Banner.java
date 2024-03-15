package com.dgmoonlabs.cms.domain.banner.entity;

import com.dgmoonlabs.cms.domain.banner.constant.BannerType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Banner extends BaseEntity {
    @Id
    private Long id;

    @Comment("배너 타입")
    private BannerType type;

    @Column(nullable = false)
    @Comment("배너 순서")
    private int order;

    @Comment("배너 이름")
    private String name;

    @Comment("배너 설명")
    private String description;

    @Comment("링크")
    private String link;

    @Comment("새창 열기 여부")
    private boolean opensNewWindow;

    @Comment("게시 시작 일시")
    private LocalDateTime startDateTime;

    @Comment("게시 종료 일시")
    private LocalDateTime endDateTime;

    @Comment("사용 여부")
    private boolean isUsing;

    @Comment("가로 길이")
    private Integer width;

    @Comment("세로 길이")
    private Integer height;

    @Comment("CSS 위치 top 값")
    private Integer top;

    @Comment("CSS 위치 left")
    private Integer left;

    @Comment("파일 ID")
    private Long fileId;
}
