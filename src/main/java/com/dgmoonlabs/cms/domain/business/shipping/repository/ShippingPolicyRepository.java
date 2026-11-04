package com.dgmoonlabs.cms.domain.business.shipping.repository;

import com.dgmoonlabs.cms.domain.business.shipping.entity.ShippingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingPolicyRepository extends JpaRepository<ShippingPolicy, Long> {
    Optional<ShippingPolicy> findFirstByOrderByIdAsc();
}
