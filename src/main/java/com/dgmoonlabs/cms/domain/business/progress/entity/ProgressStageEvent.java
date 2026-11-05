package com.dgmoonlabs.cms.domain.business.progress.entity;

import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/** 대상 하나가 어떤 단계를 거쳤는지 쌓이는 기록. 가장 최근 것이 지금 단계다. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ProgressStageEvent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("대상")
    private ProgressTargetType targetType;

    @Column(nullable = false)
    @Comment("대상 ID")
    private Long targetId;

    @Column(nullable = false, length = 50)
    @Comment("단계 코드")
    private String stageCode;

    @Column(nullable = false, length = 100)
    @Comment("단계 이름 (당시 이름을 남겨 둠)")
    private String stageLabel;

    @Column(length = 1000)
    @Comment("메모")
    private String memo;

    @Column(length = 100)
    @Comment("처리한 관리자 이름")
    private String actorName;
}
