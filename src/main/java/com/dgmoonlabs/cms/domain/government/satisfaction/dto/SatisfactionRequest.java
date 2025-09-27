package com.dgmoonlabs.cms.domain.government.satisfaction.dto;

import com.dgmoonlabs.cms.domain.government.satisfaction.entity.Satisfaction;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class SatisfactionRequest {
    private Long id;
    private Long menuId;
    private Long userId;
    private int fivePoint;
    private int fourPoint;
    private int threePoint;
    private int twoPoint;
    private int onePoint;

    public Satisfaction toEntity() {
        return Satisfaction.builder()
                .id(id)
                .menuId(menuId)
                .fivePoint(fivePoint)
                .fourPoint(fourPoint)
                .threePoint(threePoint)
                .twoPoint(twoPoint)
                .onePoint(onePoint)
                .build();
    }
}
