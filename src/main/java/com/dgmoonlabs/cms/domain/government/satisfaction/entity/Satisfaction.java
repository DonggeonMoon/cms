package com.dgmoonlabs.cms.domain.government.satisfaction.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Satisfaction extends BaseEntity {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private Long menuId;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int fivePoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int fourPoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int threePoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int twoPoint;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int onePoint;
}
