package com.dgmoonlabs.cms.domain.business.discount.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Discount extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("제품 ID")
    private Long productId;

    @Column(nullable = false)
    @Comment("이름")
    private String name;

    @Comment("설명")
    private String description;

    @Comment("할인율")
    private BigDecimal rate;
}
