package com.dgmoonlabs.cms.domain.business.reservation.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationType {
    PROGRAM("프로그램", Kind.TIME),
    CONSULTATION("상담", Kind.TIME),
    RENTAL("대관", Kind.SPACE),
    LODGING("숙박·대실", Kind.SPACE),
    EVENT("공연·행사", Kind.SPACE),
    ITEM_RENTAL("물품 대여", Kind.ITEM),
    VISIT("출장·방문", Kind.TIME),
    TABLE("식당 테이블", Kind.SPACE),
    TOUR("여행·투어", Kind.TIME);

    public enum Kind {
        TIME, SPACE, ITEM
    }

    private final String label;
    private final Kind kind;

    public boolean usesSlots() {
        return this == CONSULTATION || this == RENTAL || this == VISIT || this == TABLE;
    }

    public boolean usesLayout() {
        return kind == Kind.SPACE;
    }
}
