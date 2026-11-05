package com.dgmoonlabs.cms.domain.business.progress.repository;

import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import com.dgmoonlabs.cms.domain.business.progress.entity.ProgressStageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressStageTemplateRepository extends JpaRepository<ProgressStageTemplate, Long> {
    List<ProgressStageTemplate> findAllByTargetTypeOrderBySequenceAsc(ProgressTargetType targetType);

    boolean existsByTargetTypeAndCode(ProgressTargetType targetType, String code);
}
