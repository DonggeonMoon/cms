package com.dgmoonlabs.cms.domain.content.controller;

import com.dgmoonlabs.cms.domain.content.dto.ContentResponse;
import com.dgmoonlabs.cms.domain.content.dto.ContentSaveRequest;
import com.dgmoonlabs.cms.domain.content.dto.ContentSummaryResponse;
import com.dgmoonlabs.cms.domain.content.service.ContentService;
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
public class ContentController {
    private final ContentService contentService;

    @GetMapping("/api/contents")
    public ResponseEntity<List<ContentSummaryResponse>> publishedList() {
        return ResponseEntity.ok(contentService.listPublishedContents());
    }

    @GetMapping("/api/contents/{id}")
    public ResponseEntity<ContentResponse> published(@PathVariable final Long id) {
        return ResponseEntity.ok(contentService.getPublishedContent(id));
    }

    @GetMapping("/api/admin/contents")
    public ResponseEntity<PagedModel<ContentResponse>> search(@RequestParam(required = false) final String keyword,
                                                              @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(contentService.searchContents(keyword, pageable)));
    }

    @GetMapping("/api/admin/contents/{id}")
    public ResponseEntity<ContentResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(contentService.getContent(id));
    }

    @PostMapping("/api/admin/contents")
    public ResponseEntity<Void> create(@RequestBody @Valid final ContentSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/contents/" + contentService.createContent(request))).build();
    }

    @PutMapping("/api/admin/contents/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final ContentSaveRequest request) {
        contentService.updateContent(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/contents/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }
}
