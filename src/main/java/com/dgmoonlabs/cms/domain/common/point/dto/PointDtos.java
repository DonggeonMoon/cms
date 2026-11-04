package com.dgmoonlabs.cms.domain.common.point.dto;

import com.dgmoonlabs.cms.domain.common.point.constant.PointType;
import com.dgmoonlabs.cms.domain.common.point.entity.PointLedger;
import com.dgmoonlabs.cms.domain.common.point.entity.PointPolicy;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PointDtos {
    private PointDtos() {
    }

    public record PointPolicyRequest(boolean isEnabled,
                                     @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal earnRate,
                                     @Min(1) @Max(3650) int expireDays,
                                     @Min(0) long minUsePoints,
                                     @Min(0) @Max(100) int maxUseRate,
                                     @Min(0) long signupPoints) {
    }

    public record PointPolicyResponse(boolean isEnabled, BigDecimal earnRate, int expireDays, long minUsePoints, int maxUseRate,
                                      long signupPoints) {
        public static PointPolicyResponse from(final PointPolicy policy) {
            return new PointPolicyResponse(policy.isEnabled(), policy.getEarnRate(), policy.getExpireDays(),
                    policy.getMinUsePoints(), policy.getMaxUseRate(), policy.getSignupPoints());
        }
    }

    public record PointAdjustRequest(@NotNull Long amount, @NotBlank @Size(max = 200) String memo) {
    }

    public record PointSummary(long balance, long expiringSoon, LocalDateTime expiringBy) {
    }

    public record PointLedgerResponse(Long id, PointType type, long amount, long balanceAfter, LocalDateTime expiresAt,
                                      Long orderId, String memo, LocalDateTime createdAt) {
        public static PointLedgerResponse from(final PointLedger ledger) {
            return new PointLedgerResponse(ledger.getId(), ledger.getType(), ledger.getAmount(), ledger.getBalanceAfter(),
                    ledger.getAmount() > 0 ? ledger.getExpiresAt() : null, ledger.getOrderId(), ledger.getMemo(),
                    ledger.getCreatedAt());
        }
    }
}
