package com.dgmoonlabs.cms.domain.banner.service;

import com.dgmoonlabs.cms.domain.banner.constant.BannerType;
import com.dgmoonlabs.cms.domain.banner.dto.BannerResponse;
import com.dgmoonlabs.cms.domain.banner.dto.BannerSaveRequest;
import com.dgmoonlabs.cms.domain.banner.entity.Banner;
import com.dgmoonlabs.cms.domain.banner.repository.BannerRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {
    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public Page<BannerResponse> searchBanners(final String keyword, final BannerType type, final Pageable pageable) {
        return bannerRepository.search(keyword, type, pageable).map(BannerResponse::from);
    }

    @Transactional(readOnly = true)
    public List<BannerResponse> getActiveBanners(final BannerType type) {
        return bannerRepository.findActive(type, LocalDateTime.now()).stream().map(BannerResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BannerResponse getBanner(final Long id) {
        return BannerResponse.from(findBanner(id));
    }

    @Transactional
    public Long createBanner(final BannerSaveRequest request) {
        validatePeriod(request);
        return bannerRepository.save(request.toEntity()).getId();
    }

    @Transactional
    public void updateBanner(final Long id, final BannerSaveRequest request) {
        validatePeriod(request);
        findBanner(id).update(request);
    }

    @Transactional
    public void deleteBanner(final Long id) {
        findBanner(id).delete();
    }

    private void validatePeriod(final BannerSaveRequest request) {
        if (!request.hasValidPeriod()) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
    }

    private Banner findBanner(final Long id) {
        return bannerRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}
