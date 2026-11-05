package com.dgmoonlabs.cms.domain.business.progress.repository;

import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import com.dgmoonlabs.cms.domain.business.progress.entity.ProgressStageEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressStageEventRepository extends JpaRepository<ProgressStageEvent, Long> {
    List<ProgressStageEvent> findAllByTargetTypeAndTargetIdOrderByIdAsc(ProgressTargetType targetType, Long targetId);

    Optional<ProgressStageEvent> findFirstByTargetTypeAndTargetIdOrderByIdDesc(ProgressTargetType targetType, Long targetId);
}
