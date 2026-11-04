package com.dgmoonlabs.cms.domain.business.digital.controller;

import com.dgmoonlabs.cms.domain.business.digital.dto.DigitalDtos.AccessResponse;
import com.dgmoonlabs.cms.domain.business.digital.service.DigitalAccessService;
import com.dgmoonlabs.cms.domain.common.file.service.FileService.DownloadableFile;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class DigitalAccessController {
    private final DigitalAccessService digitalAccessService;

    @GetMapping("/api/digital/me")
    public ResponseEntity<PagedModel<AccessResponse>> mine(@AuthenticationPrincipal final LoginUser user,
                                                           @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(digitalAccessService.getMine(user.id(), pageable)));
    }

    @GetMapping("/api/digital/{accessId}/files/{fileId}")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long accessId,
                                             @PathVariable final Long fileId) {
        DownloadableFile file = digitalAccessService.download(user.id(), accessId, fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.filename(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(file.resource());
    }
}
