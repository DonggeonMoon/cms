package com.dgmoonlabs.cms.domain.business.reservation.dto;

import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ReservationRequest {
    private Long id;
}
