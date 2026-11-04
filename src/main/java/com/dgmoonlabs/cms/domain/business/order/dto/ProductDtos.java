package com.dgmoonlabs.cms.domain.business.order.dto;

import com.dgmoonlabs.cms.domain.business.order.constant.BillingCycle;
import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.discount.dto.DiscountDtos.DiscountResponse;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.entity.product.ProductOption;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record OptionRequest(
            Long id,
            @Size(max = 100) String name,
            @Size(max = 255) String description,
            @NotNull @PositiveOrZero BigDecimal additionalPrice,
            @Size(max = 5) List<@NotBlank @Size(max = 50) String> optionValues,
            @PositiveOrZero Integer stock,
            Boolean isEnabled
    ) {
        public boolean isEnabledOrDefault() {
            return isEnabled == null || isEnabled;
        }
    }

    public record OptionGroupRequest(@NotBlank @Size(max = 30) String name,
                                     @NotEmpty @Size(max = 30) List<@NotBlank @Size(max = 50) String> values) {
        public Product.OptionGroup toGroup() {
            return new Product.OptionGroup(name.trim(), values.stream().map(String::trim).toList());
        }
    }

    @Builder
    public record ProductSaveRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 500) String description,
            Long imageFileId,
            @NotNull @PositiveOrZero BigDecimal price,
            ProductSaleType saleType,
            BillingCycle billingCycle,
            @Size(max = 10) List<@Positive Integer> donationPresets,
            @PositiveOrZero BigDecimal goalAmount,
            @PositiveOrZero Integer availableQuantity,
            Long accessGradeId,
            Long grantGradeId,
            @Positive @Max(36_500) Integer grantGradeDays,
            @Size(max = 200_000) String contentHtml,
            boolean isEnabled,
            @Valid @Size(max = 5) List<OptionGroupRequest> optionGroups,
            @Valid @Size(max = 300) List<OptionRequest> options,
            Boolean requiresShipping,
            @Size(max = 20) List<@NotNull Long> digitalFileIds,
            @Positive @Max(10_000) Integer downloadLimit,
            @Positive @Max(36_500) Integer downloadDays
    ) {
        public List<Long> digitalFileIdsOrEmpty() {
            return digitalFileIds == null ? List.of() : digitalFileIds.stream().distinct().toList();
        }

        public List<OptionGroupRequest> optionGroupsOrEmpty() {
            return optionGroups == null ? List.of() : optionGroups;
        }

        public boolean requiresShippingOrDefault() {
            return requiresShipping == null || requiresShipping;
        }

        public List<OptionRequest> optionsOrEmpty() {
            return options == null ? List.of() : options;
        }

        public ProductSaleType saleTypeOrDefault() {
            return saleType == null ? ProductSaleType.PURCHASE : saleType;
        }

        public String donationPresetsText() {
            return donationPresets == null || donationPresets.isEmpty() ? null
                    : donationPresets.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
    }

    public record OptionResponse(Long id, String name, String description, BigDecimal additionalPrice,
                                 List<String> optionValues, Integer stock, boolean isEnabled) {
        public static OptionResponse from(final ProductOption option) {
            return new OptionResponse(option.getId(), option.getName(), option.getDescription(), option.getAdditionalPrice(),
                    option.getOptionValues() == null ? List.of() : option.getOptionValues(), option.getStock(), option.isEnabled());
        }
    }

    public record ProductSummary(Long id, String name, String description, BigDecimal price, ProductSaleType saleType,
                                 BillingCycle billingCycle, Integer availableQuantity, boolean isEnabled, String imageUrl,
                                 Long accessGradeId, Long grantGradeId, Integer grantGradeDays, LocalDateTime createdAt) {
        public static ProductSummary from(final Product product) {
            return new ProductSummary(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                    product.getSaleType(), product.getBillingCycle(), product.getAvailableQuantity(), product.isEnabled(),
                    toImageUrl(product.getImageFileId()), product.getAccessGradeId(), product.getGrantGradeId(),
                    product.getGrantGradeDays(), product.getCreatedAt());
        }
    }

    public record DonationProgress(BigDecimal raisedAmount, long supporterCount) {
    }

    public record ProductDetail(Long id, String name, String description, BigDecimal price, ProductSaleType saleType,
                                BillingCycle billingCycle, List<Integer> donationPresets, BigDecimal goalAmount,
                                DonationProgress donation, Integer availableQuantity, boolean isEnabled, Long imageFileId,
                                String imageUrl, String contentHtml, List<OptionResponse> options,
                                List<DiscountResponse> discounts, Double averageRating, Long accessGradeId,
                                Long grantGradeId, Integer grantGradeDays, List<Product.OptionGroup> optionGroups,
                                boolean requiresShipping, int digitalFileCount, Integer downloadLimit, Integer downloadDays,
                                List<FileResponse> digitalFiles) {
        public static ProductDetail of(final Product product, final List<OptionResponse> options,
                                       final List<DiscountResponse> discounts, final Double averageRating,
                                       final DonationProgress donation, final List<FileResponse> digitalFiles) {
            List<Integer> presets = product.getDonationPresets() == null || product.getDonationPresets().isBlank() ? List.of()
                    : Arrays.stream(product.getDonationPresets().split(",")).map(String::trim).map(Integer::valueOf).toList();
            return new ProductDetail(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                    product.getSaleType(), product.getBillingCycle(), presets, product.getGoalAmount(), donation,
                    product.getAvailableQuantity(), product.isEnabled(), product.getImageFileId(),
                    toImageUrl(product.getImageFileId()), product.getContentHtml(), options, discounts, averageRating,
                    product.getAccessGradeId(), product.getGrantGradeId(), product.getGrantGradeDays(),
                    product.getOptionGroups() == null ? List.of() : product.getOptionGroups(), product.isRequiresShipping(),
                    product.hasDigitalFiles() ? product.getDigitalFileIds().size() : 0, product.getDownloadLimit(),
                    product.getDownloadDays(), digitalFiles);
        }
    }

    static String toImageUrl(final Long fileId) {
        return fileId == null ? null : "/api/files/" + fileId + "/view";
    }
}
