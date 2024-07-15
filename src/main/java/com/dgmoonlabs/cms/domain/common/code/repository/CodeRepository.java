package com.dgmoonlabs.cms.domain.common.code.repository;

import com.dgmoonlabs.cms.domain.common.code.entity.Code;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeRepository extends JpaRepository<Code, Long>, CodeCustomRepository {
}
