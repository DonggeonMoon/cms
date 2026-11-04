package com.dgmoonlabs.cms.domain.business.digital.dto;

import com.dgmoonlabs.cms.domain.business.digital.entity.DigitalAccess;

import java.time.LocalDateTime;
import java.util.List;

public final class DigitalDtos {
    private DigitalDtos() {
    }

    public record DigitalFile(Long id, String name, Long size, String downloadUrl) {
    }

    public record AccessResponse(Long id, Long orderId, Long productId, String productName, List<DigitalFile> files,
                                 Integer downloadLimit, int downloadCount, Integer remainingDownloads,
                                 LocalDateTime expiresAt, LocalDateTime revokedAt, boolean usable, LocalDateTime createdAt) {
        public static AccessResponse of(final DigitalAccess access, final List<DigitalFile> files, final LocalDateTime now) {
            return new AccessResponse(access.getId(), access.getOrderId(), access.getProductId(), access.getProductName(), files,
                    access.getDownloadLimit(), access.getDownloadCount(), access.remainingDownloads(), access.getExpiresAt(),
                    access.getRevokedAt(), access.isUsableAt(now), access.getCreatedAt());
        }
    }
}
