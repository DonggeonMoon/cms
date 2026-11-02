package com.dgmoonlabs.cms.domain.map.controller;

import com.dgmoonlabs.cms.domain.map.dto.MapDtos.*;
import com.dgmoonlabs.cms.domain.map.service.MapService;
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
public class MapController {
    private final MapService mapService;

    @GetMapping("/api/map")
    public ResponseEntity<PublicMapResponse> publicMap() {
        return ResponseEntity.ok(mapService.getPublicMap());
    }

    @GetMapping("/api/admin/map/settings")
    public ResponseEntity<MapSettingResponse> setting() {
        return ResponseEntity.ok(mapService.getSetting());
    }

    @PutMapping("/api/admin/map/settings")
    public ResponseEntity<MapSettingResponse> updateSetting(@RequestBody @Valid final MapSettingRequest request) {
        return ResponseEntity.ok(mapService.updateSetting(request));
    }

    @GetMapping("/api/admin/map/branches")
    public ResponseEntity<PagedModel<BranchResponse>> branches(@RequestParam(required = false) final String keyword,
                                                               @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(mapService.searchBranches(keyword, pageable)));
    }

    @GetMapping("/api/admin/map/branches/{id}")
    public ResponseEntity<BranchResponse> branch(@PathVariable final Long id) {
        return ResponseEntity.ok(mapService.getBranch(id));
    }

    @PostMapping("/api/admin/map/branches")
    public ResponseEntity<Void> createBranch(@RequestBody @Valid final BranchSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/map/branches/" + mapService.createBranch(request))).build();
    }

    @PutMapping("/api/admin/map/branches/{id}")
    public ResponseEntity<Void> updateBranch(@PathVariable final Long id, @RequestBody @Valid final BranchSaveRequest request) {
        mapService.updateBranch(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/map/branches/{id}")
    public ResponseEntity<Void> deleteBranch(@PathVariable final Long id) {
        mapService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }
}
