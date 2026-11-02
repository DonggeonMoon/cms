package com.dgmoonlabs.cms.domain.admin.config.repository;

import com.dgmoonlabs.cms.domain.admin.config.entity.Config;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfigRepository extends JpaRepository<Config, Long> {
    Optional<Config> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByKeyAndDeletedAtIsNull(String key);

    Page<Config> findAllByDeletedAtIsNullAndKeyContainingIgnoreCaseOrderByKeyAsc(String keyword, Pageable pageable);

    List<Config> findAllByIsHiddenFalseAndDeletedAtIsNull();
}
