package com.dgmoonlabs.cms.domain.media.controller;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.media.dto.MediaDtos.*;
import com.dgmoonlabs.cms.domain.media.service.MediaService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import com.dgmoonlabs.cms.domain.common.file.service.FileService.DownloadableFile;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.net.URI;

@RestController
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @GetMapping("/api/media")
    public ResponseEntity<PagedModel<MediaResponse>> list(@RequestParam(required = false) final String keyword,
                                                          @RequestParam(required = false) final MediaType mediaType,
                                                          @PageableDefault(size = 12) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(mediaService.getMediaList(keyword, mediaType, pageable)));
    }

    @GetMapping("/api/media/{id:\\d+}")
    public ResponseEntity<MediaResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(mediaService.getMedia(id));
    }

    @GetMapping("/api/media/{id:\\d+}/stream")
    public ResponseEntity<Resource> stream(@PathVariable final Long id) {
        DownloadableFile file = mediaService.stream(id);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(file.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(file.filename(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(file.resource());
    }

    @PostMapping("/api/media/{id:\\d+}/start")
    public ResponseEntity<MyProgress> start(@PathVariable final Long id, @AuthenticationPrincipal final LoginUser user) {
        return ResponseEntity.ok(mediaService.start(id, user));
    }

    @PostMapping("/api/media/{id:\\d+}/progress")
    public ResponseEntity<MyProgress> progress(@PathVariable final Long id, @AuthenticationPrincipal final LoginUser user,
                                               @RequestBody @Valid final ProgressRequest request) {
        return ResponseEntity.ok(mediaService.record(id, user, request));
    }

    @GetMapping("/api/media/me")
    public ResponseEntity<PagedModel<MyMedia>> myMedia(@AuthenticationPrincipal final LoginUser user,
                                                            @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(mediaService.getMyMedia(user.id(), pageable)));
    }

    @GetMapping("/api/admin/media")
    public ResponseEntity<PagedModel<MediaResponse>> search(@RequestParam(required = false) final String keyword,
                                                            @RequestParam(required = false) final MediaType mediaType,
                                                            @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(mediaService.searchMedia(keyword, mediaType, pageable)));
    }

    @PostMapping("/api/admin/media")
    public ResponseEntity<Void> create(@RequestBody @Valid final MediaSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/media/" + mediaService.createMedia(request))).build();
    }

    @PutMapping("/api/admin/media/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final MediaSaveRequest request) {
        mediaService.updateMedia(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/media/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/media/{id}/learners")
    public ResponseEntity<PagedModel<LearnerResponse>> learners(@PathVariable final Long id,
                                                                @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(mediaService.getLearners(id, pageable)));
    }
}
