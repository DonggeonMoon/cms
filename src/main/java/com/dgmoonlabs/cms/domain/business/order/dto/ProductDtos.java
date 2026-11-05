package com.dgmoonlabs.cms.domain.business.order.dto;

import com.dgmoonlabs.cms.domain.business.category.dto.CategoryDtos.Spec;
import com.dgmoonlabs.cms.domain.business.order.constant.BillingCycle;
import com.dgmoonlabs.cms.domain.business.order.constant.PassUnit;
import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.discount.dto.DiscountDtos.DiscountResponse;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.entity.product.ProductOption;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
            @Positive @Max(600) Integer commitmentMonths,
            @Positive @Max(100) Integer earlyTerminationFeeRate,
            @Size(max = 10) List<@Positive Integer> donationPresets,
            @PositiveOrZero BigDecimal goalAmount,
            LocalDateTime fundingDeadline,
            PassUnit passUnit,
            @Positive @Max(3_650) Integer passDays,
            @Positive @Max(100_000) Integer passCount,
            Long passProgramId,
            Long inquiryProgramId,
            LocalDateTime preorderSaleStartAt,
            LocalDateTime preorderSaleEndAt,
            LocalDate preorderShipDate,
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
            @Positive @Max(36_500) Integer downloadDays,
            Long categoryId,
            @Size(max = 30) Map<String, Object> attributes,
            Boolean storeOrder
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
                                 BillingCycle billingCycle, Integer commitmentMonths, Integer earlyTerminationFeeRate,
                                 Integer availableQuantity, boolean isEnabled, String imageUrl,
                                 Long accessGradeId, Long grantGradeId, Integer grantGradeDays, Long categoryId,
                                 boolean storeOrder, LocalDateTime createdAt) {
        public static ProductSummary from(final Product product) {
            return new ProductSummary(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                    product.getSaleType(), product.getBillingCycle(), product.getCommitmentMonths(), product.getEarlyTerminationFeeRate(),
                    product.getAvailableQuantity(), product.isEnabled(),
                    toImageUrl(product.getImageFileId()), product.getAccessGradeId(), product.getGrantGradeId(),
                    product.getGrantGradeDays(), product.getCategoryId(), product.isStoreOrder(), product.getCreatedAt());
        }
    }

    public record PassInfo(PassUnit unit, Integer days, Integer count, Long programId) {
        public static PassInfo of(final Product product) {
            return product.getPassUnit() == null ? null
                    : new PassInfo(product.getPassUnit(), product.getPassDays(), product.getPassCount(), product.getPassProgramId());
        }
    }

    public record InquiryInfo(Long programId) {
        public static InquiryInfo of(final Product product) {
            return product.getSaleType() == ProductSaleType.INQUIRY ? new InquiryInfo(product.getInquiryProgramId()) : null;
        }
    }

    public record PreorderInfo(LocalDateTime saleStartAt, LocalDateTime saleEndAt, LocalDate shipDate) {
        public static PreorderInfo of(final Product product) {
            return product.getSaleType() != ProductSaleType.PREORDER ? null
                    : new PreorderInfo(product.getPreorderSaleStartAt(), product.getPreorderSaleEndAt(), product.getPreorderShipDate());
        }
    }

    public record DonationProgress(BigDecimal raisedAmount, long supporterCount) {
    }

    public record FundingInfo(BigDecimal goalAmount, LocalDateTime deadline, BigDecimal raisedAmount, long supporterCount,
                              boolean resolved, boolean succeeded) {
    }

    public record ProductDetail(Long id, String name, String description, BigDecimal price, ProductSaleType saleType,
                                BillingCycle billingCycle, Integer commitmentMonths, Integer earlyTerminationFeeRate,
                                List<Integer> donationPresets, BigDecimal goalAmount,
                                PassInfo pass, InquiryInfo inquiry, PreorderInfo preorder, DonationProgress donation, FundingInfo funding,
                                Integer availableQuantity,
                                boolean isEnabled, Long imageFileId, String imageUrl, String contentHtml, List<OptionResponse> options,
                                List<DiscountResponse> discounts, Double averageRating, Long accessGradeId,
                                Long grantGradeId, Integer grantGradeDays, List<Product.OptionGroup> optionGroups,
                                boolean requiresShipping, int digitalFileCount, Integer downloadLimit, Integer downloadDays,
                                List<FileResponse> digitalFiles, Long categoryId, Map<String, Object> attributes,
                                List<Spec> specs, boolean storeOrder) {
        public static ProductDetail of(final Product product, final List<OptionResponse> options,
                                       final List<DiscountResponse> discounts, final Double averageRating,
                                       final DonationProgress donation, final FundingInfo funding, final List<FileResponse> digitalFiles,
                                       final Map<String, Object> attributes, final List<Spec> specs) {
            List<Integer> presets = product.getDonationPresets() == null || product.getDonationPresets().isBlank() ? List.of()
                    : Arrays.stream(product.getDonationPresets().split(",")).map(String::trim).map(Integer::valueOf).toList();
            return new ProductDetail(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                    product.getSaleType(), product.getBillingCycle(), product.getCommitmentMonths(), product.getEarlyTerminationFeeRate(),
                    presets, product.getGoalAmount(), PassInfo.of(product), InquiryInfo.of(product),
                    PreorderInfo.of(product), donation, funding, product.getAvailableQuantity(), product.isEnabled(), product.getImageFileId(),
                    toImageUrl(product.getImageFileId()), product.getContentHtml(), options, discounts, averageRating,
                    product.getAccessGradeId(), product.getGrantGradeId(), product.getGrantGradeDays(),
                    product.getOptionGroups() == null ? List.of() : product.getOptionGroups(), product.isRequiresShipping(),
                    product.hasDigitalFiles() ? product.getDigitalFileIds().size() : 0, product.getDownloadLimit(),
                    product.getDownloadDays(), digitalFiles, product.getCategoryId(), attributes, specs, product.isStoreOrder());
        }
    }

    static String toImageUrl(final Long fileId) {
        return fileId == null ? null : "/api/files/" + fileId + "/view";
    }
}
