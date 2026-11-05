package com.dgmoonlabs.cms.domain.business.repair.dto;

import com.dgmoonlabs.cms.domain.business.repair.entity.RepairRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class RepairDtos {
    private RepairDtos() {
    }

    public record RepairSaveRequest(@NotBlank @Size(max = 200) String title, @NotBlank @Size(max = 2000) String description,
                                    Long productId, @Size(max = 10) List<@NotNull Long> photoFileIds) {
        public List<Long> photoFileIdsOrEmpty() {
            return photoFileIds == null ? List.of() : photoFileIds.stream().distinct().toList();
        }
    }

    public record QuoteOrderRequest(@NotNull Long productId) {
    }

    public record RepairResponse(Long id, Long memberId, Long productId, String title, String description,
                                 List<String> photoUrls, Long quoteOrderId, LocalDateTime createdAt) {
        public static RepairResponse from(final RepairRequest repair) {
            List<Long> photos = repair.getPhotoFileIds() == null ? List.of() : repair.getPhotoFileIds();
            return new RepairResponse(repair.getId(), repair.getMemberId(), repair.getProductId(), repair.getTitle(),
                    repair.getDescription(), photos.stream().map(id -> "/api/files/" + id + "/view").toList(),
                    repair.getQuoteOrderId(), repair.getCreatedAt());
        }
    }
}
