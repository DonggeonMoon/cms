package com.dgmoonlabs.cms.domain.admin.apikey.repository;

import com.dgmoonlabs.cms.domain.admin.apikey.entity.ApiKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    Page<ApiKey> findAllByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByNameAsc(String keyword, Pageable pageable);

    List<ApiKey> findAllByDeletedAtIsNullOrderByNameAsc();
}
