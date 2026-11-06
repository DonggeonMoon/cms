package com.dgmoonlabs.cms.domain.admin.site.controller;

import com.dgmoonlabs.cms.domain.admin.site.dto.SiteResponse;
import com.dgmoonlabs.cms.domain.admin.site.dto.SiteSaveRequest;
import com.dgmoonlabs.cms.domain.admin.site.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class SiteApiController {
    private final SiteService siteService;

    @GetMapping("/api/site")
    public ResponseEntity<SiteResponse> currentSite() {
        return ResponseEntity.ok(siteService.getCurrentSite());
    }

    @GetMapping("/api/admin/sites")
    public ResponseEntity<PagedModel<SiteResponse>> search(@RequestParam(required = false) final String keyword,
                                                           @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(siteService.searchSites(keyword, pageable)));
    }

    @GetMapping("/api/admin/sites/{id}")
    public ResponseEntity<SiteResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(siteService.getSite(id));
    }

    @PostMapping("/api/admin/sites")
    public ResponseEntity<Void> create(@RequestBody @Valid final SiteSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/sites/" + siteService.createSite(request))).build();
    }

    @PutMapping("/api/admin/sites/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final SiteSaveRequest request) {
        siteService.updateSite(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/sites/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        siteService.deleteSite(id);
        return ResponseEntity.noContent().build();
    }
}
