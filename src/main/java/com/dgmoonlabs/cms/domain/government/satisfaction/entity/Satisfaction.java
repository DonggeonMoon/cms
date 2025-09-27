package com.dgmoonlabs.cms.domain.government.satisfaction.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Satisfaction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Comment("메뉴 ID")
    private Long menuId;

    @Comment("사용자 고유식별번호")
    private Long userId;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("5점")
    private Integer fivePoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("4점")
    private Integer fourPoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("3점")
    private Integer threePoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("2점")
    private Integer twoPoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("1점")
    private Integer onePoint;

    public void update(Integer five, Integer four, Integer three, Integer two, Integer one) {
        this.fivePoint = five;
        this.fourPoint = four;
        this.threePoint = three;
        this.twoPoint = two;
        this.onePoint = one;
    }
}
