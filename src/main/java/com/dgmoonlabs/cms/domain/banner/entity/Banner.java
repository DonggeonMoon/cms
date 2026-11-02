package com.dgmoonlabs.cms.domain.banner.entity;

import com.dgmoonlabs.cms.domain.banner.constant.BannerType;
import com.dgmoonlabs.cms.domain.banner.dto.BannerSaveRequest;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Banner extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("배너 타입")
    private BannerType type;

    @Column(name = "`order`", nullable = false)
    @Comment("배너 순서")
    private int order;

    @Comment("배너 이름")
    private String name;

    @Comment("배너 설명")
    private String description;

    @Comment("링크")
    private String link;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("새창 열기 여부")
    private boolean opensNewWindow;

    @Comment("게시 시작 일시")
    private LocalDateTime startDateTime;

    @Comment("게시 종료 일시")
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("사용 여부")
    private boolean isUsing;

    @Comment("가로 길이")
    private Integer width;

    @Comment("세로 길이")
    private Integer height;

    @Comment("CSS 위치 top 값")
    private Integer top;

    @Column(name = "`left`")
    @Comment("CSS 위치 left")
    private Integer left;

    @Comment("파일 ID")
    private Long fileId;

    @Column(columnDefinition = "varchar(7)")
    @Comment("띠배너 배경색 (#RRGGBB)")
    private String backgroundColor;

    @Column(columnDefinition = "varchar(7)")
    @Comment("띠배너 글자색 (#RRGGBB)")
    private String textColor;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("팝업 모달 여부 (true: 배경 클릭 불가, false: 모달리스)")
    private boolean isModal;

    @Column(nullable = false)
    @ColumnDefault("1")
    @Comment("보지 않기로 닫았을 때 다시 보이지 않는 일수 (0: 보지 않기 버튼 없음)")
    private int dismissDays;

    public void update(final BannerSaveRequest request) {
        this.type = request.type();
        this.order = request.order();
        this.name = request.name();
        this.description = request.description();
        this.link = request.link();
        this.opensNewWindow = request.opensNewWindow();
        this.startDateTime = request.startDateTime();
        this.endDateTime = request.endDateTime();
        this.isUsing = request.isUsing();
        this.width = request.width();
        this.height = request.height();
        this.top = request.top();
        this.left = request.left();
        this.fileId = request.fileId();
        this.backgroundColor = request.backgroundColor();
        this.textColor = request.textColor();
        this.isModal = request.isModal();
        this.dismissDays = request.dismissDaysOrDefault();
    }
}
