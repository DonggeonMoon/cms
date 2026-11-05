package com.dgmoonlabs.cms.domain.business.marketing.entity;

import com.dgmoonlabs.cms.domain.common.notification.constant.NotificationChannel;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/** 대상 회원에게 보낸 마케팅 발송 이력. 발송은 즉시 이뤄지고, 이 행은 기록용이다. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MarketingCampaign extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Comment("제목")
    private String title;

    @Column(nullable = false, length = 2000)
    @Comment("내용")
    private String content;

    @Comment("대상 등급 ID (비우면 전체 회원)")
    private Long targetGradeId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("발송 채널")
    private List<NotificationChannel> channels;

    @Column(nullable = false)
    @Comment("발송 대상 수")
    private int recipientCount;

    @Column(nullable = false)
    @Comment("발송 일시")
    private LocalDateTime sentAt;
}
