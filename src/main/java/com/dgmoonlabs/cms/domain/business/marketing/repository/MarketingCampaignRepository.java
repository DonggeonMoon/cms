package com.dgmoonlabs.cms.domain.business.marketing.repository;

import com.dgmoonlabs.cms.domain.business.marketing.entity.MarketingCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketingCampaignRepository extends JpaRepository<MarketingCampaign, Long> {
    Page<MarketingCampaign> findAllByOrderByIdDesc(Pageable pageable);
}
