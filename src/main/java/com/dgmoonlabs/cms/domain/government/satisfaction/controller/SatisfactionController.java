package com.dgmoonlabs.cms.domain.government.satisfaction.controller;

import com.dgmoonlabs.cms.domain.government.satisfaction.dto.SatisfactionDtos.OpinionResponse;
import com.dgmoonlabs.cms.domain.government.satisfaction.dto.SatisfactionDtos.SatisfactionResponse;
import com.dgmoonlabs.cms.domain.government.satisfaction.dto.SatisfactionDtos.VoteRequest;
import com.dgmoonlabs.cms.domain.government.satisfaction.service.SatisfactionService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SatisfactionController {
    private final SatisfactionService satisfactionService;

    @GetMapping("/api/satisfactions")
    public ResponseEntity<SatisfactionResponse> get(@RequestParam final Long menuId) {
        return ResponseEntity.ok(satisfactionService.getSatisfaction(menuId));
    }

    @PostMapping("/api/satisfactions")
    public ResponseEntity<SatisfactionResponse> vote(@AuthenticationPrincipal final LoginUser user,
                                                     @RequestBody @Valid final VoteRequest request) {
        return ResponseEntity.ok(satisfactionService.vote(user, request));
    }

    @GetMapping("/api/admin/satisfactions")
    public ResponseEntity<List<SatisfactionResponse>> all() {
        return ResponseEntity.ok(satisfactionService.getAll());
    }

    @GetMapping("/api/admin/satisfactions/{menuId}/opinions")
    public ResponseEntity<PagedModel<OpinionResponse>> opinions(@PathVariable final Long menuId,
                                                                @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(satisfactionService.getOpinions(menuId, pageable)));
    }

    @DeleteMapping("/api/admin/satisfactions/{menuId}")
    public ResponseEntity<Void> reset(@PathVariable final Long menuId) {
        satisfactionService.reset(menuId);
        return ResponseEntity.noContent().build();
    }
}
