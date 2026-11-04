package com.dgmoonlabs.cms.domain.common.point.repository;

import com.dgmoonlabs.cms.domain.common.point.entity.PointPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {
    Optional<PointPolicy> findFirstByOrderByIdAsc();
}
