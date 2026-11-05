package com.dgmoonlabs.cms.domain.business.repair.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

/** 수리·AS 접수 한 건. 진행 단계·메시지는 공통 기능(ProgressTargetType.REPAIR)으로 다루고, 견적은 연결한 주문(QUOTE 상품)으로 결제받는다. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class RepairRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("접수한 회원 ID")
    private Long memberId;

    @Comment("관련 상품 ID (어떤 상품을 산 건지, 없으면 비움)")
    private Long productId;

    @Column(nullable = false, length = 200)
    @Comment("제목")
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    @Comment("증상·요청 내용")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("첨부 사진 파일 ID 목록")
    private List<Long> photoFileIds;

    @Comment("견적을 보낸 주문 ID (QUOTE 상품 주문, 결제는 주문에서 받음)")
    private Long quoteOrderId;

    public void linkQuoteOrder(final Long orderId) {
        this.quoteOrderId = orderId;
    }
}
