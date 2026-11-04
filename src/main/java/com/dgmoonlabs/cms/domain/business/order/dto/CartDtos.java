package com.dgmoonlabs.cms.domain.business.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {
    private CartDtos() {
    }

    public record CartAddRequest(@NotNull Long productId, Long productOptionId, @NotNull @Min(1) @Max(999) Integer quantity) {
    }

    public record CartQuantityRequest(@NotNull @Min(1) @Max(999) Integer quantity) {
    }

    public record CartItemResponse(Long id, Long productId, Long productOptionId, String productName, String optionName,
                                   String imageUrl, BigDecimal unitPrice, int quantity, BigDecimal amount,
                                   Integer stock, boolean requiresShipping, boolean available) {
    }

    public record CartResponse(List<CartItemResponse> items, BigDecimal itemsAmount, BigDecimal shippingFee,
                               String shippingRegion, BigDecimal freeShippingThreshold, BigDecimal totalPrice) {
    }
}
