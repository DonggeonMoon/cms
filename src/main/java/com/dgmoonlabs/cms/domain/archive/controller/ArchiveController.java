package com.dgmoonlabs.cms.domain.archive.controller;

import com.dgmoonlabs.cms.domain.archive.dto.ArchiveDtos.*;
import com.dgmoonlabs.cms.domain.archive.service.ArchiveService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArchiveController {
    private final ArchiveService archiveService;

    @GetMapping("/api/archives")
    public ResponseEntity<List<ArchiveResponse>> archives() {
        return ResponseEntity.ok(archiveService.getArchives());
    }

    @GetMapping("/api/archives/{archiveId}")
    public ResponseEntity<ArchiveResponse> archive(@PathVariable final Long archiveId) {
        return ResponseEntity.ok(archiveService.getArchive(archiveId));
    }

    @GetMapping("/api/archives/{archiveId}/articles")
    public ResponseEntity<PagedModel<ArchiveArticleSummary>> articles(@PathVariable final Long archiveId,
                                                                      @RequestParam(required = false) final String keyword,
                                                                      @RequestParam(defaultValue = "0") final int page) {
        return ResponseEntity.ok(new PagedModel<>(archiveService.getArticles(archiveId, keyword, page)));
    }

    @GetMapping("/api/archives/{archiveId}/articles/{articleId}")
    public ResponseEntity<ArchiveArticleDetail> article(@PathVariable final Long archiveId, @PathVariable final Long articleId) {
        return ResponseEntity.ok(archiveService.getArticle(archiveId, articleId));
    }

    @GetMapping("/api/archives/{archiveId}/articles/{articleId}/comments")
    public ResponseEntity<List<ArchiveCommentResponse>> comments(@PathVariable final Long archiveId,
                                                                 @PathVariable final Long articleId) {
        return ResponseEntity.ok(archiveService.getComments(archiveId, articleId));
    }

    @PostMapping("/api/archives/{archiveId}/articles/{articleId}/comments")
    public ResponseEntity<Void> createComment(@PathVariable final Long archiveId, @PathVariable final Long articleId,
                                              @RequestBody @Valid final ArchiveCommentSaveRequest request,
                                              final HttpServletRequest httpRequest) {
        Long id = archiveService.createComment(archiveId, articleId, request, httpRequest.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/archives/" + archiveId + "/articles/" + articleId + "/comments/" + id))
                .build();
    }

    @DeleteMapping("/api/archives/{archiveId}/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable final Long archiveId, @PathVariable final Long articleId,
                                              @PathVariable final Long commentId) {
        archiveService.deleteComment(archiveId, articleId, commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/archives")
    public ResponseEntity<Void> createArchive(@RequestBody @Valid final ArchiveSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/archives/" + archiveService.createArchive(request))).build();
    }

    @PutMapping("/api/admin/archives/{archiveId}")
    public ResponseEntity<Void> updateArchive(@PathVariable final Long archiveId, @RequestBody @Valid final ArchiveSaveRequest request) {
        archiveService.updateArchive(archiveId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/archives/{archiveId}")
    public ResponseEntity<Void> deleteArchive(@PathVariable final Long archiveId) {
        archiveService.deleteArchive(archiveId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/archives/{archiveId}/articles")
    public ResponseEntity<Void> createArticle(@PathVariable final Long archiveId,
                                              @RequestBody @Valid final ArchiveArticleSaveRequest request,
                                              final HttpServletRequest httpRequest) {
        Long id = archiveService.createArticle(archiveId, request, httpRequest.getRemoteAddr());
        return ResponseEntity.created(URI.create("/api/archives/" + archiveId + "/articles/" + id)).build();
    }

    @PutMapping("/api/admin/archives/{archiveId}/articles/{articleId}")
    public ResponseEntity<Void> updateArticle(@PathVariable final Long archiveId, @PathVariable final Long articleId,
                                              @RequestBody @Valid final ArchiveArticleSaveRequest request) {
        archiveService.updateArticle(archiveId, articleId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/archives/{archiveId}/articles/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable final Long archiveId, @PathVariable final Long articleId) {
        archiveService.deleteArticle(archiveId, articleId);
        return ResponseEntity.noContent().build();
    }
}
