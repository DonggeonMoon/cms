package com.dgmoonlabs.cms.domain.business.status.repository;

import com.dgmoonlabs.cms.domain.business.status.constant.StatusDomain;
import com.dgmoonlabs.cms.domain.business.status.entity.StatusDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatusDefinitionRepository extends JpaRepository<StatusDefinition, Long> {
    List<StatusDefinition> findAllByDomainAndDeletedAtIsNullOrderBySystemDescSequenceAscIdAsc(StatusDomain domain);

    Optional<StatusDefinition> findByDomainAndCode(StatusDomain domain, String code);

    Optional<StatusDefinition> findByIdAndDeletedAtIsNull(Long id);
}
