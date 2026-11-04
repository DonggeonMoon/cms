package com.dgmoonlabs.cms.domain.business.shipping.dto;

import com.dgmoonlabs.cms.domain.business.shipping.entity.ShippingPolicy;
import com.dgmoonlabs.cms.domain.business.shipping.entity.ShippingPolicy.RegionFee;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public final class ShippingDtos {
    private ShippingDtos() {
    }

    public record RegionFeeRequest(@NotBlank @Size(max = 30) String name,
                                   @NotBlank @Pattern(regexp = "\\d{5}") String postalCodeFrom,
                                   @NotBlank @Pattern(regexp = "\\d{5}") String postalCodeTo,
                                   @NotNull @PositiveOrZero BigDecimal fee) {
        public RegionFee toRegionFee() {
            return postalCodeFrom.compareTo(postalCodeTo) <= 0
                    ? new RegionFee(name.trim(), postalCodeFrom, postalCodeTo, fee)
                    : new RegionFee(name.trim(), postalCodeTo, postalCodeFrom, fee);
        }
    }

    public record ShippingPolicyRequest(@NotNull @PositiveOrZero BigDecimal baseFee,
                                        @PositiveOrZero BigDecimal freeThreshold,
                                        @Valid @Size(max = 50) List<RegionFeeRequest> regionFees,
                                        @Size(max = 50) String defaultCourier) {
    }

    public record ShippingPolicyResponse(BigDecimal baseFee, BigDecimal freeThreshold, List<RegionFee> regionFees,
                                         String defaultCourier) {
        public static ShippingPolicyResponse from(final ShippingPolicy policy) {
            return new ShippingPolicyResponse(policy.getBaseFee(), policy.getFreeThreshold(),
                    policy.getRegionFees() == null ? List.of() : policy.getRegionFees(), policy.getDefaultCourier());
        }
    }
}
