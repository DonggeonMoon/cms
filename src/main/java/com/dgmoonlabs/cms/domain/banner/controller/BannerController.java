package com.dgmoonlabs.cms.domain.banner.controller;

import com.dgmoonlabs.cms.domain.banner.constant.BannerType;
import com.dgmoonlabs.cms.domain.banner.dto.BannerResponse;
import com.dgmoonlabs.cms.domain.banner.dto.BannerSaveRequest;
import com.dgmoonlabs.cms.domain.banner.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BannerController {
    private final BannerService bannerService;

    @GetMapping("/api/banners")
    public ResponseEntity<List<BannerResponse>> active(@RequestParam(required = false) final BannerType type) {
        return ResponseEntity.ok(bannerService.getActiveBanners(type));
    }

    @GetMapping("/api/admin/banners")
    public ResponseEntity<PagedModel<BannerResponse>> search(@RequestParam(required = false) final String keyword,
                                                             @RequestParam(required = false) final BannerType type,
                                                             @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(bannerService.searchBanners(keyword, type, pageable)));
    }

    @GetMapping("/api/admin/banners/{id}")
    public ResponseEntity<BannerResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(bannerService.getBanner(id));
    }

    @PostMapping("/api/admin/banners")
    public ResponseEntity<Void> create(@RequestBody @Valid final BannerSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/banners/" + bannerService.createBanner(request))).build();
    }

    @PutMapping("/api/admin/banners/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final BannerSaveRequest request) {
        bannerService.updateBanner(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/banners/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }
}
