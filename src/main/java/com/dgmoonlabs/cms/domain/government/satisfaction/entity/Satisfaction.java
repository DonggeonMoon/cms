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

    public static Satisfaction empty(final Long menuId) {
        return Satisfaction.builder().menuId(menuId).fivePoint(0).fourPoint(0).threePoint(0).twoPoint(0).onePoint(0).build();
    }

    public void update(Integer five, Integer four, Integer three, Integer two, Integer one) {
        this.fivePoint = five;
        this.fourPoint = four;
        this.threePoint = three;
        this.twoPoint = two;
        this.onePoint = one;
    }

    public void add(final int score) {
        switch (score) {
            case 5 -> fivePoint++;
            case 4 -> fourPoint++;
            case 3 -> threePoint++;
            case 2 -> twoPoint++;
            case 1 -> onePoint++;
            default -> throw new IllegalArgumentException("score must be 1..5");
        }
    }

    public int total() {
        return fivePoint + fourPoint + threePoint + twoPoint + onePoint;
    }

    public double average() {
        int total = total();
        if (total == 0) {
            return 0;
        }
        return (5.0 * fivePoint + 4.0 * fourPoint + 3.0 * threePoint + 2.0 * twoPoint + onePoint) / total;
    }
}
