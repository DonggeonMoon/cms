package com.dgmoonlabs.cms.domain.common.regex.repository;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.entity.RegexBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegexBindingRepository extends JpaRepository<RegexBinding, Long> {
    Optional<RegexBinding> findByTarget(RegexTarget target);
}
