package com.dgmoonlabs.cms.domain.admin.menu.repository;

import com.dgmoonlabs.cms.domain.admin.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByIdAndSiteIdAndDeletedAtIsNull(Long id, Long siteId);

    List<Menu> findAllBySiteIdAndDeletedAtIsNull(Long siteId);

    boolean existsByParentIdAndSiteIdAndDeletedAtIsNull(Long parentId, Long siteId);
}
