package com.dgmoonlabs.cms.domain.business.order.dto;

import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.validation.MatchesRegex;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class OrderDtos {
    private OrderDtos() {
    }

    @Builder
    public record OrderCreateRequest(
            @NotNull Long productId,
            Long productOptionId,
            @NotNull @Min(1) @Max(999) Integer quantity,
            Long discountId,
            @NotBlank @Size(max = 50) String receiverName,
            @NotBlank @Size(max = 30) @MatchesRegex(RegexTarget.PHONE_NUMBER) String phoneNumber,
            @Size(max = 10) @MatchesRegex(RegexTarget.POSTAL_CODE) String postalCode,
            @Size(max = 255) String address,
            @Size(max = 255) String detailAddress
    ) {
    }

    public record OrderStatusRequest(@NotNull OrderStatus status) {
    }

    public record OrderResponse(Long id, String orderNumber, Long userId, Long productId, String productName,
                                Long productOptionId, int quantity, Long discountId, BigDecimal totalPrice,
                                OrderStatus status, String receiverName, String phoneNumber, String postalCode,
                                String address, String detailAddress, LocalDateTime createdAt) {
        public static OrderResponse from(final Order order) {
            return new OrderResponse(order.getId(), order.getOrderNumber(), order.getUserId(), order.getProductId(),
                    order.getProductName(), order.getProductOptionId(), order.getQuantity(), order.getDiscountId(),
                    order.getTotalPrice(), order.getStatus(), order.getReceiverName(), order.getPhoneNumber(),
                    order.getPostalCode(), order.getAddress(), order.getDetailAddress(), order.getCreatedAt());
        }
    }
}
