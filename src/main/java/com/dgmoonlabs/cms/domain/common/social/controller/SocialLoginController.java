package com.dgmoonlabs.cms.domain.common.social.controller;

import com.dgmoonlabs.cms.domain.common.social.constant.SocialProvider;
import com.dgmoonlabs.cms.domain.common.social.dto.SocialDtos.*;
import com.dgmoonlabs.cms.domain.common.social.service.SocialLoginService;
import com.dgmoonlabs.cms.domain.common.social.service.SocialLoginService.CallbackResult;
import com.dgmoonlabs.cms.domain.common.social.service.SocialLoginService.LoggedIn;
import com.dgmoonlabs.cms.domain.common.social.service.SocialLoginSettingService;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.security.RequiresReauthentication;
import com.dgmoonlabs.cms.global.security.SessionLoginHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SocialLoginController {
    private final SocialLoginService socialLoginService;
    private final SocialLoginSettingService settingService;
    private final SessionLoginHandler sessionLoginHandler;

    @GetMapping("/api/auth/oauth/providers")
    public ResponseEntity<List<ProviderOption>> providers() {
        return ResponseEntity.ok(settingService.getEnabledProviders());
    }

    @GetMapping("/api/auth/oauth/{provider}/authorize")
    public ResponseEntity<Void> authorize(@PathVariable final String provider,
                                          @RequestParam(required = false) final String redirect,
                                          final HttpServletRequest request) {
        String url = socialLoginService.authorizationUrl(provider(provider), redirect, request.getSession(true));
        return redirectTo(url, HttpStatus.FOUND);
    }

    @GetMapping("/api/auth/oauth/{provider}/callback")
    public ResponseEntity<Void> callback(@PathVariable final String provider,
                                         @RequestParam(required = false) final String code,
                                         @RequestParam(required = false) final String state,
                                         @RequestParam(required = false) final String error,
                                         @RequestParam(required = false) final String user,
                                         final HttpServletRequest request, final HttpServletResponse response) {
        CallbackResult result = socialLoginService.callback(provider(provider), code, state, error, user, request.getSession(true));
        if (result instanceof LoggedIn loggedIn) {
            sessionLoginHandler.login(loggedIn.user(), request, response);
        }
        return redirectTo(result.redirect(), HttpStatus.FOUND);
    }

    // 애플의 cross-site POST 에는 SameSite=Lax 세션 쿠키가 실리지 않아 같은 콜백의 GET 으로 넘긴다.
    @PostMapping(value = "/api/auth/oauth/apple/callback", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Void> appleFormPost(@RequestParam(required = false) final String code,
                                              @RequestParam(required = false) final String state,
                                              @RequestParam(required = false) final String error,
                                              @RequestParam(required = false) final String user) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/api/auth/oauth/apple/callback");
        addIfPresent(uri, "code", code);
        addIfPresent(uri, "state", state);
        addIfPresent(uri, "error", error);
        addIfPresent(uri, "user", user);
        return redirectTo(uri.encode().build().toUriString(), HttpStatus.SEE_OTHER);
    }

    @GetMapping("/api/auth/oauth/signup")
    public ResponseEntity<PendingSignupResponse> pendingSignup(final HttpServletRequest request) {
        return ResponseEntity.ok(socialLoginService.pendingSignup(request.getSession(false)));
    }

    @PostMapping("/api/auth/oauth/signup")
    public ResponseEntity<SignupResult> signup(@RequestBody @Valid final SocialJoinRequest body,
                                               final HttpServletRequest request, final HttpServletResponse response) {
        LoggedIn loggedIn = socialLoginService.signup(body, request.getSession(false));
        sessionLoginHandler.login(loggedIn.user(), request, response);
        return ResponseEntity.ok(new SignupResult(loggedIn.redirect()));
    }

    @RequiresReauthentication
    @GetMapping("/api/admin/social-logins")
    public ResponseEntity<List<SocialLoginSettingResponse>> settings() {
        return ResponseEntity.ok(settingService.getSettings());
    }

    @RequiresReauthentication
    @PutMapping("/api/admin/social-logins/{provider}")
    public ResponseEntity<SocialLoginSettingResponse> update(@PathVariable final String provider,
                                                             @RequestBody @Valid final SocialLoginSettingRequest request) {
        return ResponseEntity.ok(settingService.update(provider(provider), request));
    }

    private static SocialProvider provider(final String path) {
        SocialProvider provider = SocialProvider.fromPath(path);
        if (provider == null) {
            throw BusinessException.notFound();
        }
        return provider;
    }

    private static void addIfPresent(final UriComponentsBuilder uri, final String name, final String value) {
        if (value != null) {
            uri.queryParam(name, value);
        }
    }

    private static ResponseEntity<Void> redirectTo(final String location, final HttpStatus status) {
        return ResponseEntity.status(status).header(HttpHeaders.LOCATION, URI.create(location).toString()).build();
    }
}
