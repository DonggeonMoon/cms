package com.dgmoonlabs.cms.domain.banner.repository;

import com.dgmoonlabs.cms.domain.banner.dto.BannerRequest;
import com.dgmoonlabs.cms.domain.banner.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BannerCustomRepository {
    Page<Banner> find(BannerRequest bannerRequest, Pageable pageable);

    List<Banner> find(BannerRequest bannerRequest);
}
