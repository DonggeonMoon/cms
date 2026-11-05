package com.dgmoonlabs.cms.domain.business.license.dto;

import com.dgmoonlabs.cms.domain.business.license.entity.LicenseKey;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class LicenseKeyDtos {
    private LicenseKeyDtos() {
    }

    public record BulkAddRequest(@NotNull Long productId, @NotEmpty List<String> codes) {
    }

    public record BulkAddResult(int added, int duplicate) {
    }

    public record LicenseKeyResponse(Long id, Long productId, String code, Long orderId, Long memberId,
                                     LocalDateTime assignedAt, LocalDateTime createdAt) {
        public static LicenseKeyResponse from(final LicenseKey entity) {
            return new LicenseKeyResponse(entity.getId(), entity.getProductId(), entity.getCode(), entity.getOrderId(),
                    entity.getMemberId(), entity.getAssignedAt(), entity.getCreatedAt());
        }
    }

    public record MyLicenseKeyResponse(Long id, Long productId, String productName, String code, Long orderId,
                                       LocalDateTime assignedAt) {
    }
}
