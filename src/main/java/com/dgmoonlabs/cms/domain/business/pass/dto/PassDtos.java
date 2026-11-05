package com.dgmoonlabs.cms.domain.business.pass.dto;

import com.dgmoonlabs.cms.domain.business.order.constant.PassUnit;
import com.dgmoonlabs.cms.domain.business.pass.constant.PassStatus;
import com.dgmoonlabs.cms.domain.business.pass.entity.MemberPass;

import java.time.LocalDateTime;

public final class PassDtos {
    private PassDtos() {
    }

    public record PassResponse(Long id, Long memberId, Long productId, String productName, PassUnit unit, Long programId,
                               Integer totalCount, Integer remainingCount, LocalDateTime expiresAt, PassStatus status,
                               LocalDateTime createdAt) {
        public static PassResponse from(final MemberPass pass) {
            return new PassResponse(pass.getId(), pass.getMemberId(), pass.getProductId(), pass.getProductName(),
                    pass.getUnit(), pass.getProgramId(), pass.getTotalCount(), pass.getRemainingCount(),
                    pass.getExpiresAt(), pass.getStatus(), pass.getCreatedAt());
        }
    }
}
