package com.dgmoonlabs.cms.domain.business.order.dto;

import com.dgmoonlabs.cms.domain.business.order.constant.OrderReturnStatus;
import com.dgmoonlabs.cms.domain.business.order.constant.OrderReturnType;
import com.dgmoonlabs.cms.domain.business.order.entity.OrderReturn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class OrderReturnDtos {
    private OrderReturnDtos() {
    }

    public record OrderReturnCreateRequest(@NotNull OrderReturnType type, @NotBlank @Size(max = 1000) String reason,
                                           Long exchangeOptionId) {
    }

    public record OrderReturnDecisionRequest(@Size(max = 1000) String adminMemo) {
    }

    public record OrderReturnResponse(Long id, Long orderId, String orderNumber, Long memberId, OrderReturnType type,
                                      String reason, Long exchangeOptionId, OrderReturnStatus status, String adminMemo,
                                      LocalDateTime processedAt, LocalDateTime createdAt) {
        public static OrderReturnResponse of(final OrderReturn entity, final String orderNumber) {
            return new OrderReturnResponse(entity.getId(), entity.getOrderId(), orderNumber, entity.getMemberId(),
                    entity.getType(), entity.getReason(), entity.getExchangeOptionId(), entity.getStatus(),
                    entity.getAdminMemo(), entity.getProcessedAt(), entity.getCreatedAt());
        }
    }
}
