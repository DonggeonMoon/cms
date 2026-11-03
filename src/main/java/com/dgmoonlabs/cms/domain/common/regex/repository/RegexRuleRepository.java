package com.dgmoonlabs.cms.domain.common.regex.repository;

import com.dgmoonlabs.cms.domain.common.regex.entity.RegexRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegexRuleRepository extends JpaRepository<RegexRule, Long> {
    Optional<RegexRule> findByIdAndDeletedAtIsNull(Long id);

    Optional<RegexRule> findByRuleKey(String ruleKey);

    List<RegexRule> findAllByDeletedAtIsNullOrderByIdAsc();
}
