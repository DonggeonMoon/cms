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

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("5점")
    private int fivePoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("4점")
    private int fourPoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("3점")
    private int threePoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("2점")
    private int twoPoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("1점")
    private int onePoint;
}
