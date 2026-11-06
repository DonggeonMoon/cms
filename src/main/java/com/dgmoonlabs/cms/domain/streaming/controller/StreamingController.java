package com.dgmoonlabs.cms.domain.streaming.controller;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.streaming.dto.StreamingDtos.*;
import com.dgmoonlabs.cms.domain.streaming.service.StreamingService;
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
public class StreamingController {
    private final StreamingService streamingService;

    @GetMapping("/api/streaming")
    public ResponseEntity<PagedModel<StreamingResponse>> list(@RequestParam(required = false) final String keyword,
                                                               @RequestParam(required = false) final MediaType mediaType,
                                                               @PageableDefault(size = 12) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(streamingService.getStreamingList(keyword, mediaType, pageable)));
    }

    @GetMapping("/api/streaming/{id:\\d+}")
    public ResponseEntity<StreamingResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(streamingService.getStreaming(id));
    }

    @GetMapping("/api/streaming/{id:\\d+}/stream")
    public ResponseEntity<Resource> stream(@PathVariable final Long id) {
        DownloadableFile file = streamingService.stream(id);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(file.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(file.filename(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(file.resource());
    }

    @PostMapping("/api/streaming/{id:\\d+}/start")
    public ResponseEntity<MyProgress> start(@PathVariable final Long id, @AuthenticationPrincipal final LoginUser user) {
        return ResponseEntity.ok(streamingService.start(id, user));
    }

    @PostMapping("/api/streaming/{id:\\d+}/progress")
    public ResponseEntity<MyProgress> progress(@PathVariable final Long id, @AuthenticationPrincipal final LoginUser user,
                                               @RequestBody @Valid final ProgressRequest request) {
        return ResponseEntity.ok(streamingService.record(id, user, request));
    }

    @GetMapping("/api/streaming/me")
    public ResponseEntity<PagedModel<MyStreaming>> myStreaming(@AuthenticationPrincipal final LoginUser user,
                                                                @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(streamingService.getMyStreaming(user.id(), pageable)));
    }

    @GetMapping("/api/admin/streaming")
    public ResponseEntity<PagedModel<StreamingResponse>> search(@RequestParam(required = false) final String keyword,
                                                                 @RequestParam(required = false) final MediaType mediaType,
                                                                 @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(streamingService.searchStreaming(keyword, mediaType, pageable)));
    }

    @PostMapping("/api/admin/streaming")
    public ResponseEntity<Void> create(@RequestBody @Valid final StreamingSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/streaming/" + streamingService.createStreaming(request))).build();
    }

    @PutMapping("/api/admin/streaming/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final StreamingSaveRequest request) {
        streamingService.updateStreaming(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/streaming/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        streamingService.deleteStreaming(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/streaming/{id}/learners")
    public ResponseEntity<PagedModel<LearnerResponse>> learners(@PathVariable final Long id,
                                                                 @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(streamingService.getLearners(id, pageable)));
    }
}
