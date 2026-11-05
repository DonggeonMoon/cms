package com.dgmoonlabs.cms.domain.business.progress.entity;

import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/** 대상별로 관리자가 정해 둔 진행 단계 하나. 순서대로 보여 준다. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ProgressStageTemplate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("대상 (주문, AS 등)")
    private ProgressTargetType targetType;

    @Column(nullable = false, length = 50)
    @Comment("단계 코드")
    private String code;

    @Column(nullable = false, length = 100)
    @Comment("단계 이름")
    private String label;

    @Column(nullable = false)
    @Comment("표시 순서")
    private int sequence;

    public void update(final String label, final int sequence) {
        this.label = label;
        this.sequence = sequence;
    }
}
