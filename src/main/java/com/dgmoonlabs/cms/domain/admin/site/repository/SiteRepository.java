package com.dgmoonlabs.cms.domain.admin.site.repository;

import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long>, SiteCustomRepository {
    Optional<Site> findByIdAndDeletedAtIsNull(Long id);

    Optional<Site> findFirstByIsDefaultTrueAndDeletedAtIsNullOrderByIdAsc();

    List<Site> findAllByIsDefaultTrueAndDeletedAtIsNull();

    Optional<Site> findByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndDeletedAtIsNullAndIdNot(String code, Long id);

    boolean existsByCodeAndDeletedAtIsNull(String code);
}
