package com.dgmoonlabs.cms.domain.common.user.controller;

import com.dgmoonlabs.cms.domain.common.user.dto.MemberJoinRequest;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberModifyRequest;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberResponse;
import com.dgmoonlabs.cms.domain.common.user.dto.PasswordChangeRequest;
import com.dgmoonlabs.cms.domain.common.user.service.MemberService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import com.dgmoonlabs.cms.global.security.SessionLoginHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final SessionLoginHandler sessionLoginHandler;

    @PostMapping
    public ResponseEntity<Void> join(@RequestBody @Valid final MemberJoinRequest request) {
        return ResponseEntity.created(URI.create("/api/members/" + memberService.join(request))).build();
    }

    @GetMapping("/username-availability")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam final String username) {
        return ResponseEntity.ok(Map.of("available", memberService.isUsernameAvailable(username)));
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(@AuthenticationPrincipal final LoginUser loginUser) {
        return ResponseEntity.ok(memberService.getMember(loginUser.id()));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> modify(@AuthenticationPrincipal final LoginUser loginUser,
                                       @RequestBody @Valid final MemberModifyRequest request) {
        memberService.modify(loginUser.id(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal final LoginUser loginUser,
                                               @RequestBody @Valid final PasswordChangeRequest request) {
        memberService.changePassword(loginUser.id(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> quit(@AuthenticationPrincipal final LoginUser loginUser,
                                     final HttpServletRequest request, final HttpServletResponse response) {
        memberService.quit(loginUser.id());
        sessionLoginHandler.logout(request, response);
        return ResponseEntity.noContent().build();
    }
}
