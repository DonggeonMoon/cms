package com.dgmoonlabs.cms.domain.common.file.controller;

import com.dgmoonlabs.cms.domain.common.file.constant.MediaType;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
public class FileAdminController {
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<PagedModel<FileResponse>> search(@RequestParam(required = false) final String keyword,
                                                           @RequestParam(required = false) final MediaType mediaType,
                                                           @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(fileService.searchFiles(keyword, mediaType, pageable)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
