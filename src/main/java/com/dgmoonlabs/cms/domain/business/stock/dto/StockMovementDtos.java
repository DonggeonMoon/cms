package com.dgmoonlabs.cms.domain.business.stock.dto;

import com.dgmoonlabs.cms.domain.business.stock.constant.StockMovementType;
import com.dgmoonlabs.cms.domain.business.stock.entity.StockMovement;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class StockMovementDtos {
    private StockMovementDtos() {
    }

    public record AdjustRequest(@NotNull Long productId, Long productOptionId, @NotNull Integer delta, String note) {
    }

    public record StockMovementResponse(Long id, Long productId, Long productOptionId, StockMovementType type,
                                         int quantity, Long orderId, Integer quantityAfter, String note,
                                         LocalDateTime createdAt) {
        public static StockMovementResponse from(final StockMovement entity) {
            return new StockMovementResponse(entity.getId(), entity.getProductId(), entity.getProductOptionId(),
                    entity.getType(), entity.getQuantity(), entity.getOrderId(), entity.getQuantityAfter(),
                    entity.getNote(), entity.getCreatedAt());
        }
    }
}
