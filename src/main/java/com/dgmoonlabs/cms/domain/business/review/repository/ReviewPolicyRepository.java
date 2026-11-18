package com.dgmoonlabs.cms.domain.business.review.repository;

import com.dgmoonlabs.cms.domain.business.review.entity.ReviewPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewPolicyRepository extends JpaRepository<ReviewPolicy, Long> {
    Optional<ReviewPolicy> findFirstByOrderByIdAsc();
}
