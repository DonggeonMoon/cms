package com.dgmoonlabs.cms.domain.common.code.controller;

import com.dgmoonlabs.cms.domain.common.code.dto.CodeRequest;
import com.dgmoonlabs.cms.domain.common.code.dto.CodeResponse;
import com.dgmoonlabs.cms.domain.common.code.dto.CodeSaveRequest;
import com.dgmoonlabs.cms.domain.common.code.service.CodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/codes")
@RequiredArgsConstructor
public class CodeAdminController {
    private final CodeService codeService;

    @GetMapping
    public ResponseEntity<PagedModel<CodeResponse>> search(@ModelAttribute final CodeRequest request,
                                                           @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(codeService.getCodes(request, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(codeService.getCode(id));
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid final CodeSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/codes/" + codeService.createCode(request))).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final CodeSaveRequest request) {
        codeService.updateCode(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        codeService.deleteCode(id);
        return ResponseEntity.noContent().build();
    }
}
