package com.dgmoonlabs.cms.domain.common.user.controller;

import com.dgmoonlabs.cms.domain.common.user.dto.MemberResponse;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberSummaryResponse;
import com.dgmoonlabs.cms.domain.common.user.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class MemberAdminController {
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<PagedModel<MemberSummaryResponse>> search(@RequestParam(required = false) final String keyword,
                                                                    @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(memberService.searchMembers(keyword, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(memberService.getMember(id));
    }

    @PostMapping("/{id}/unlock")
    public ResponseEntity<Void> unlock(@PathVariable final Long id) {
        memberService.unlock(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        memberService.quit(id);
        return ResponseEntity.noContent().build();
    }
}
