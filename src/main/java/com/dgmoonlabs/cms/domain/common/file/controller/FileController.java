package com.dgmoonlabs.cms.domain.common.file.controller;

import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.file.service.FileService.DownloadableFile;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponse> upload(@RequestPart("file") final MultipartFile file,
                                               @RequestParam(required = false) final String alternativeText) {
        FileResponse response = fileService.upload(file, alternativeText);
        return ResponseEntity.created(URI.create("/api/files/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(fileService.getFile(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable final Long id) {
        return serve(fileService.download(id), ContentDisposition.attachment());
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> view(@PathVariable final Long id) {
        return serve(fileService.view(id), ContentDisposition.inline());
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> thumbnail(@PathVariable final Long id) {
        return serve(fileService.thumbnail(id), ContentDisposition.inline());
    }

    private ResponseEntity<Resource> serve(final DownloadableFile file, final ContentDisposition.Builder disposition) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        disposition.filename(file.filename(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.resource());
    }
}
