package com.dgmoonlabs.cms.domain.banner.repository;

import com.dgmoonlabs.cms.domain.banner.constant.BannerType;
import com.dgmoonlabs.cms.domain.banner.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BannerCustomRepository {
    Page<Banner> search(String keyword, BannerType type, Pageable pageable);

    List<Banner> findActive(BannerType type, LocalDateTime now);
}
