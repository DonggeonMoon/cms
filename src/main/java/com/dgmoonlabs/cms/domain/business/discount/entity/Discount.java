package com.dgmoonlabs.cms.domain.business.discount.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    private Long id;

    @Comment("제품 ID")
    private Long productId;

    @Column(nullable = false)
    @Comment("이름")
    private String name;

    @Comment("설명")
    private String description;

    private BigDecimal rate;
}
