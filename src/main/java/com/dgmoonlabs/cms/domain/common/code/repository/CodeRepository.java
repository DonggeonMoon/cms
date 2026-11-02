package com.dgmoonlabs.cms.domain.common.code.repository;

import com.dgmoonlabs.cms.domain.common.code.entity.Code;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeRepository extends JpaRepository<Code, Long>, CodeCustomRepository {
    Optional<Code> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByParentIdAndDeletedAtIsNull(Long parentId);
}
