package com.dgmoonlabs.cms.domain.banner.service;

import com.dgmoonlabs.cms.domain.banner.dto.BannerRequest;
import com.dgmoonlabs.cms.domain.banner.entity.Banner;
import com.dgmoonlabs.cms.domain.banner.repository.BannerRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BannerService {
    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public Page<Banner> getBanner(BannerRequest banner, Pageable pageable) {
        return bannerRepository.find(banner, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Banner> getBannerWithoutPaging(Banner banner, Pageable pageable) {
        return bannerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Banner getBanner(long id) {
        return bannerRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Transactional
    public void updateBanner(HttpServletRequest request) {

    }
}
