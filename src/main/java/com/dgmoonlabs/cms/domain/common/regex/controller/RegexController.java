package com.dgmoonlabs.cms.domain.common.regex.controller;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.dto.RegexDtos.*;
import com.dgmoonlabs.cms.domain.common.regex.service.RegexService;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RegexController {
    private final RegexService regexService;

    @GetMapping("/api/regexes")
    public ResponseEntity<List<PublicRegex>> publicRules() {
        return ResponseEntity.ok(regexService.getPublicRules());
    }

    @GetMapping("/api/admin/regexes")
    public ResponseEntity<List<RegexResponse>> rules() {
        return ResponseEntity.ok(regexService.getRules());
    }

    @GetMapping("/api/admin/regexes/options")
    public ResponseEntity<List<RegexOption>> options() {
        return ResponseEntity.ok(regexService.getOptions());
    }

    @PostMapping("/api/admin/regexes")
    public ResponseEntity<Void> create(@RequestBody @Valid final RegexSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/regexes/" + regexService.create(request))).build();
    }

    @PutMapping("/api/admin/regexes/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final RegexSaveRequest request) {
        regexService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/regexes/test")
    public ResponseEntity<RegexTestResult> test(@RequestBody @Valid final RegexTestRequest request) {
        return ResponseEntity.ok(regexService.test(request));
    }

    @GetMapping("/api/admin/regex-bindings/{target}")
    public ResponseEntity<BindingRequest> binding(@PathVariable final RegexTarget target) {
        return ResponseEntity.ok(new BindingRequest(regexService.selected(numberTarget(target))));
    }

    @PutMapping("/api/admin/regex-bindings/{target}")
    public ResponseEntity<Void> bind(@PathVariable final RegexTarget target, @RequestBody final BindingRequest request) {
        regexService.select(numberTarget(target), request.regexRuleId());
        return ResponseEntity.noContent().build();
    }

    private static RegexTarget numberTarget(final RegexTarget target) {
        if (target.getGroup() != RegexTarget.Group.NUMBER) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return target;
    }
}
