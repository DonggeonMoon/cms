package com.dgmoonlabs.cms.domain.common.user.controller;

import com.dgmoonlabs.cms.domain.common.user.dto.MemberJoinRequest;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberModifyRequest;
import com.dgmoonlabs.cms.domain.common.user.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<Void> join(MemberJoinRequest request) {
        return ResponseEntity.created(
                URI.create(
                        "/member/" + memberService.join(request)
                )
        ).build();
    }

    @PutMapping
    public ResponseEntity<Object> modify(MemberModifyRequest request) {
        memberService.modify(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Object> quit(Long id) {
        memberService.quit(id);
        return ResponseEntity.noContent().build();
    }
}
