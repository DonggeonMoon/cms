package com.dgmoonlabs.cms.domain.common.code.controller;

import com.dgmoonlabs.cms.domain.common.code.dto.CodeResponse;
import com.dgmoonlabs.cms.domain.common.code.service.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CodeController {
    private final CodeService codeService;

    @GetMapping
    public ResponseEntity<List<CodeResponse>> getChildren(@RequestParam(required = false) final Long parentId) {
        return ResponseEntity.ok(codeService.getUsingChildren(parentId));
    }
}
