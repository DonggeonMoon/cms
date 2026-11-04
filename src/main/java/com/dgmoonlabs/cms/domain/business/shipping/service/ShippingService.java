package com.dgmoonlabs.cms.domain.business.shipping.service;

import com.dgmoonlabs.cms.domain.business.shipping.dto.ShippingDtos.RegionFeeRequest;
import com.dgmoonlabs.cms.domain.business.shipping.dto.ShippingDtos.ShippingPolicyRequest;
import com.dgmoonlabs.cms.domain.business.shipping.dto.ShippingDtos.ShippingPolicyResponse;
import com.dgmoonlabs.cms.domain.business.shipping.entity.ShippingPolicy;
import com.dgmoonlabs.cms.domain.business.shipping.repository.ShippingPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingService {
    private final ShippingPolicyRepository policyRepository;

    @Transactional(readOnly = true)
    public ShippingPolicyResponse getPolicy() {
        return ShippingPolicyResponse.from(policy());
    }

    @Transactional
    public ShippingPolicyResponse update(final ShippingPolicyRequest request) {
        ShippingPolicy policy = policyRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> policyRepository.save(ShippingPolicy.builder().build()));
        List<ShippingPolicy.RegionFee> regions = request.regionFees() == null ? List.of()
                : request.regionFees().stream().map(RegionFeeRequest::toRegionFee).toList();
        policy.update(request.baseFee(), request.freeThreshold(), regions,
                request.defaultCourier() == null || request.defaultCourier().isBlank() ? null : request.defaultCourier().trim());
        return ShippingPolicyResponse.from(policy);
    }

    @Transactional(readOnly = true)
    public ShippingPolicy.Quote quote(final boolean hasShippable, final BigDecimal shippableSubtotal, final String postalCode) {
        if (!hasShippable) {
            return new ShippingPolicy.Quote(BigDecimal.ZERO, BigDecimal.ZERO, null, BigDecimal.ZERO);
        }
        return policy().quote(shippableSubtotal, postalCode);
    }

    private ShippingPolicy policy() {
        return policyRepository.findFirstByOrderByIdAsc().orElseGet(() -> ShippingPolicy.builder().build());
    }
}
