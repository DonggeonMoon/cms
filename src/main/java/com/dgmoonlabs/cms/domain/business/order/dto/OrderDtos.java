package com.dgmoonlabs.cms.domain.business.order.dto;

import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.order.constant.OrderItemStatus;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.OrderItem;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.validation.MatchesRegex;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
            @Size(max = 255) String detailAddress,
            @Positive BigDecimal amount,
            @Size(max = 2000) String requestMessage
    ) {
    }

    public record QuoteRequest(@NotNull @PositiveOrZero BigDecimal amount, @Size(max = 2000) String message) {
    }

    public record OrderStatusRequest(@NotNull OrderStatus status) {
    }

    public record CartCheckoutRequest(
            @NotEmpty @Size(max = 100) List<@NotNull Long> cartItemIds,
            @NotBlank @Size(max = 50) String receiverName,
            @NotBlank @Size(max = 30) @MatchesRegex(RegexTarget.PHONE_NUMBER) String phoneNumber,
            @Size(max = 10) @MatchesRegex(RegexTarget.POSTAL_CODE) String postalCode,
            @Size(max = 255) String address,
            @Size(max = 255) String detailAddress
    ) {
    }

    public record ShipmentRequest(@NotBlank @Size(max = 50) String courier, @NotBlank @Size(max = 50) String trackingNumber) {
    }

    public record OrderItemResponse(Long id, Long productId, Long productOptionId, String productName, String optionName,
                                    BigDecimal unitPrice, int quantity, BigDecimal amount, OrderItemStatus status,
                                    BigDecimal refundedAmount, LocalDateTime canceledAt) {
        public static OrderItemResponse from(final OrderItem item) {
            return new OrderItemResponse(item.getId(), item.getProductId(), item.getProductOptionId(), item.getProductName(),
                    item.getOptionName(), item.getUnitPrice(), item.getQuantity(), item.getAmount(), item.getStatus(),
                    item.getRefundedAmount(), item.getCanceledAt());
        }
    }

    public record OrderResponse(Long id, String orderNumber, Long userId, Long productId, String productName,
                                ProductSaleType saleType, Long subscriptionId, Long productOptionId, int quantity,
                                Long discountId, BigDecimal totalPrice, OrderStatus status, String customStatus, String receiverName,
                                String phoneNumber, String postalCode, String address, String detailAddress,
                                String requestMessage, String quoteMessage, LocalDateTime quotedAt, LocalDateTime createdAt,
                                List<OrderItemResponse> items, BigDecimal itemsAmount, BigDecimal shippingFee,
                                BigDecimal refundedAmount, String courier, String trackingNumber, LocalDateTime shippedAt) {
        public static OrderResponse of(final Order order, final List<OrderItem> items) {
            return new OrderResponse(order.getId(), order.getOrderNumber(), order.getUserId(), order.getProductId(),
                    order.getProductName(), order.getSaleType(), order.getSubscriptionId(), order.getProductOptionId(),
                    order.getQuantity(), order.getDiscountId(), order.getTotalPrice(), order.getStatus(), order.getCustomStatus(),
                    order.getReceiverName(), order.getPhoneNumber(), order.getPostalCode(), order.getAddress(),
                    order.getDetailAddress(), order.getRequestMessage(), order.getQuoteMessage(), order.getQuotedAt(),
                    order.getCreatedAt(), items.stream().map(OrderItemResponse::from).toList(), order.getItemsAmount(),
                    order.getShippingFee(), order.getRefundedAmount(), order.getCourier(), order.getTrackingNumber(),
                    order.getShippedAt());
        }
    }
}
