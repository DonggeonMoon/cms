package com.dgmoonlabs.cms.domain.business.order.dto;

import com.dgmoonlabs.cms.domain.business.discount.dto.DiscountDtos.DiscountResponse;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.entity.product.ProductOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record OptionRequest(
            Long id,
            @NotBlank @Size(max = 100) String name,
            @Size(max = 255) String description,
            @NotNull @PositiveOrZero BigDecimal additionalPrice
    ) {
    }

    @Builder
    public record ProductSaveRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 500) String description,
            Long imageFileId,
            @NotNull @PositiveOrZero BigDecimal price,
            @PositiveOrZero Integer availableQuantity,
            Long accessGradeId,
            Long grantGradeId,
            @Positive @Max(36_500) Integer grantGradeDays,
            @Size(max = 200_000) String contentHtml,
            boolean isEnabled,
            @Valid @Size(max = 50) List<OptionRequest> options
    ) {
        public List<OptionRequest> optionsOrEmpty() {
            return options == null ? List.of() : options;
        }
    }

    public record OptionResponse(Long id, String name, String description, BigDecimal additionalPrice) {
        public static OptionResponse from(final ProductOption option) {
            return new OptionResponse(option.getId(), option.getName(), option.getDescription(), option.getAdditionalPrice());
        }
    }

    public record ProductSummary(Long id, String name, String description, BigDecimal price, Integer availableQuantity,
                                 boolean isEnabled, String imageUrl, LocalDateTime createdAt) {
        public static ProductSummary from(final Product product) {
            return new ProductSummary(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                    product.getAvailableQuantity(), product.isEnabled(), toImageUrl(product.getImageFileId()),
                    product.getCreatedAt());
        }
    }

    public record ProductDetail(Long id, String name, String description, BigDecimal price, Integer availableQuantity,
                                boolean isEnabled, Long imageFileId, String imageUrl, String contentHtml,
                                List<OptionResponse> options, List<DiscountResponse> discounts, Double averageRating) {
        public static ProductDetail of(final Product product, final List<OptionResponse> options,
                                       final List<DiscountResponse> discounts, final Double averageRating) {
            return new ProductDetail(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                    product.getAvailableQuantity(), product.isEnabled(), product.getImageFileId(),
                    toImageUrl(product.getImageFileId()), product.getContentHtml(), options, discounts, averageRating);
        }
    }

    static String toImageUrl(final Long fileId) {
        return fileId == null ? null : "/api/files/" + fileId + "/view";
    }
}
