package com.dgmoonlabs.cms.domain.common.user.controller;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.admin.crypto.service.CryptoKeyService;
import com.dgmoonlabs.cms.domain.admin.ipaccess.service.AdminIpAccessService;
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
    private final CryptoKeyService cryptoKeyService;
    private final AdminIpAccessService adminIpAccessService;

    @PostMapping("/login")
    public ResponseEntity<LoginUser> login(@RequestBody @Valid final LoginRequest request,
                                           final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) {
        // 비밀번호가 맞는지조차 알려주지 않도록 인증 전에 막는다.
        if (request.userTypeOrDefault() == UserType.ADMIN) {
            adminIpAccessService.requireAllowed(httpRequest.getRemoteAddr());
        }
        LoginUser loginUser = authService.authenticate(request);
        if (loginUser.userType() == UserType.ADMIN) {
            adminIpAccessService.requireAllowed(httpRequest.getRemoteAddr());
            cryptoKeyService.initializeIfMissing();
        }
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

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(final CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }
}
