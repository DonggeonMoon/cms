package com.dgmoonlabs.cms.domain.admin.site.repository;

import com.dgmoonlabs.cms.domain.admin.site.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, Long>, SiteCustomRepository {
}
