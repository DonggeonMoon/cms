package com.dgmoonlabs.cms.domain.business.discount.dto;

import com.dgmoonlabs.cms.domain.business.discount.entity.Discount;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

public final class DiscountDtos {
    private DiscountDtos() {
    }

    @Builder
    public record DiscountSaveRequest(
            Long productId,
            @NotBlank @Size(max = 100) String name,
            @Size(max = 255) String description,
            @NotNull @DecimalMin("0.01") @DecimalMax("100") BigDecimal rate,
            boolean isUsing
    ) {
    }

    public record DiscountResponse(Long id, Long productId, String name, String description, BigDecimal rate,
                                   boolean isUsing) {
        public static DiscountResponse from(final Discount discount) {
            return new DiscountResponse(discount.getId(), discount.getProductId(), discount.getName(),
                    discount.getDescription(), discount.getRate(), discount.isUsing());
        }
    }
}
