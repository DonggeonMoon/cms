package com.dgmoonlabs.cms.domain.postcode.controller;

import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PostcodeSearchResponse;
import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PostcodeSettingRequest;
import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PostcodeSettingResponse;
import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PublicPostcodeResponse;
import com.dgmoonlabs.cms.domain.postcode.service.PostcodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PostcodeController {
    private final PostcodeService postcodeService;

    @GetMapping("/api/postcode")
    public ResponseEntity<PublicPostcodeResponse> publicSetting() {
        return ResponseEntity.ok(postcodeService.getPublicSetting());
    }

    @GetMapping("/api/postcode/search")
    public ResponseEntity<PostcodeSearchResponse> search(@RequestParam final String keyword,
                                                          @RequestParam(defaultValue = "1") final int page) {
        return ResponseEntity.ok(postcodeService.search(keyword, page));
    }

    @GetMapping("/api/admin/postcode/settings")
    public ResponseEntity<PostcodeSettingResponse> setting() {
        return ResponseEntity.ok(postcodeService.getSetting());
    }

    @PutMapping("/api/admin/postcode/settings")
    public ResponseEntity<PostcodeSettingResponse> updateSetting(@RequestBody @Valid final PostcodeSettingRequest request) {
        return ResponseEntity.ok(postcodeService.updateSetting(request));
    }
}
