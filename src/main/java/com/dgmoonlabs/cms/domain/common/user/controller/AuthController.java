package com.dgmoonlabs.cms.domain.common.user.controller;

import com.dgmoonlabs.cms.domain.common.user.dto.LoginRequest;
import com.dgmoonlabs.cms.domain.common.user.service.AuthService;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.LoginUser;
import com.dgmoonlabs.cms.global.security.SessionLoginHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final SessionLoginHandler sessionLoginHandler;

    @PostMapping("/login")
    public ResponseEntity<LoginUser> login(@RequestBody @Valid final LoginRequest request,
                                           final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) {
        LoginUser loginUser = authService.authenticate(request);
        sessionLoginHandler.login(loginUser, httpRequest, httpResponse);
        return ResponseEntity.ok(loginUser);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(final HttpServletRequest request, final HttpServletResponse response) {
        sessionLoginHandler.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<LoginUser> me(@AuthenticationPrincipal final LoginUser loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }
        return ResponseEntity.ok(loginUser);
    }

    // touching the token makes Spring Security issue the XSRF-TOKEN cookie before the first unsafe request
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(final CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }
}
