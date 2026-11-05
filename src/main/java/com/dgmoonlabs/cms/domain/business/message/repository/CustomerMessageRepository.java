package com.dgmoonlabs.cms.domain.business.message.repository;

import com.dgmoonlabs.cms.domain.business.message.entity.CustomerMessage;
import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerMessageRepository extends JpaRepository<CustomerMessage, Long> {
    List<CustomerMessage> findAllByTargetTypeAndTargetIdOrderByIdAsc(ProgressTargetType targetType, Long targetId);
}
