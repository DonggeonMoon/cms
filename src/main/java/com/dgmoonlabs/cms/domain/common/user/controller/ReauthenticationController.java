package com.dgmoonlabs.cms.domain.common.user.controller;

import com.dgmoonlabs.cms.domain.common.user.dto.ReauthenticationRequest;
import com.dgmoonlabs.cms.domain.common.user.service.AuthService;
import com.dgmoonlabs.cms.global.security.AdminReauthentication;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReauthenticationController {
    private final AuthService authService;

    @PostMapping("/api/admin/reauthenticate")
    public ResponseEntity<Void> reauthenticate(@RequestBody @Valid final ReauthenticationRequest request,
                                               @AuthenticationPrincipal final LoginUser admin,
                                               final HttpServletRequest httpRequest) {
        authService.verifyAdminPassword(admin.id(), request.password());
        AdminReauthentication.grant(httpRequest, admin.id());
        return ResponseEntity.noContent().build();
    }
}
