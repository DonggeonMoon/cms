package com.dgmoonlabs.cms.domain.common.social.service;

import com.dgmoonlabs.cms.domain.common.social.client.SocialCredentials;
import com.dgmoonlabs.cms.domain.common.social.client.SocialOAuthClient;
import com.dgmoonlabs.cms.domain.common.social.client.SocialProfile;
import com.dgmoonlabs.cms.domain.common.social.constant.SocialProvider;
import com.dgmoonlabs.cms.domain.common.social.dto.SocialDtos.PendingSignupResponse;
import com.dgmoonlabs.cms.domain.common.social.dto.SocialDtos.SocialJoinRequest;
import com.dgmoonlabs.cms.domain.common.social.entity.SocialAccount;
import com.dgmoonlabs.cms.domain.common.social.repository.SocialAccountRepository;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.domain.common.user.service.AuthService;
import com.dgmoonlabs.cms.domain.common.user.service.MemberService;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialLoginService {
    static final String AUTHORIZATIONS = "CMS_SOCIAL_AUTHORIZATIONS";
    static final String PENDING_SIGNUP = "CMS_SOCIAL_SIGNUP";
    private static final Duration AUTHORIZATION_TTL = Duration.ofMinutes(10);
    private static final Duration SIGNUP_TTL = Duration.ofMinutes(30);
    private static final int MAX_AUTHORIZATIONS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SocialLoginSettingService settingService;
    private final SocialOAuthClient oauthClient;
    private final SocialAccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final AuthService authService;

    public record Authorization(SocialProvider provider, String redirect, String codeVerifier, String nonce,
                                Instant createdAt) implements Serializable {
    }

    public record PendingSignup(SocialProvider provider, String providerUserId, String email, String name, String nickname,
                                String mobilePhoneNumber, String redirect, Instant createdAt) implements Serializable {
    }

    public sealed interface CallbackResult {
        String redirect();
    }

    public record LoggedIn(LoginUser user, String redirect) implements CallbackResult {
    }

    public record NeedsSignup(String redirect) implements CallbackResult {
    }

    public record Failed(String redirect) implements CallbackResult {
    }

    public String authorizationUrl(final SocialProvider provider, final String redirect, final HttpSession session) {
        SocialCredentials credentials = settingService.credentials(provider);
        String state = randomToken();
        String codeVerifier = provider == SocialProvider.GOOGLE ? randomToken() : null;
        String nonce = provider == SocialProvider.APPLE ? randomToken() : null;
        saveAuthorization(session, state, new Authorization(provider, safeRedirect(redirect), codeVerifier, nonce, Instant.now()));

        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(provider.getAuthorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", credentials.clientId())
                .queryParam("redirect_uri", settingService.redirectUri(provider))
                .queryParam("state", state);
        if (!provider.getScope().isEmpty()) {
            uri.queryParam("scope", provider.getScope());
        }
        if (codeVerifier != null) {
            uri.queryParam("code_challenge", s256(codeVerifier)).queryParam("code_challenge_method", "S256");
        }
        if (nonce != null) {
            // 이름·이메일을 요청하면 애플은 결과를 POST 로 보낸다.
            uri.queryParam("response_mode", "form_post").queryParam("nonce", nonce);
        }
        return uri.encode(StandardCharsets.UTF_8).build().toUriString();
    }

    @Transactional
    public CallbackResult callback(final SocialProvider provider, final String code, final String state, final String error,
                                   final String appleUser, final HttpSession session) {
        Authorization authorization = state == null ? null : takeAuthorization(session, state);
        if (authorization == null || authorization.provider() != provider
                || authorization.createdAt().plus(AUTHORIZATION_TTL).isBefore(Instant.now())) {
            return new Failed("/login?socialError=state");
        }
        if (error != null || code == null) {
            return new Failed("/login?socialError=cancelled");
        }
        SocialProfile profile;
        try {
            profile = oauthClient.fetchProfile(provider, settingService.credentials(provider), code,
                    settingService.redirectUri(provider), state, authorization.codeVerifier(), authorization.nonce(), appleUser);
        } catch (BusinessException e) {
            return new Failed("/login?socialError=failed");
        }
        if (profile.providerUserId() == null || profile.providerUserId().isBlank()) {
            return new Failed("/login?socialError=failed");
        }

        SocialAccount account = accountRepository.findByProviderAndProviderUserId(provider, profile.providerUserId()).orElse(null);
        if (account != null) {
            try {
                LoginUser user = authService.authenticateSocialMember(account.getMemberId());
                if (user != null) {
                    return new LoggedIn(user, authorization.redirect());
                }
                accountRepository.delete(account);
            } catch (BusinessException e) {
                return new Failed("/login?socialError=locked");
            }
        }
        session.setAttribute(PENDING_SIGNUP, new PendingSignup(provider, profile.providerUserId(), profile.email(),
                profile.name(), profile.nickname(), profile.mobilePhoneNumber(), authorization.redirect(), Instant.now()));
        return new NeedsSignup("/join/social");
    }

    public PendingSignupResponse pendingSignup(final HttpSession session) {
        PendingSignup pending = requirePending(session);
        return new PendingSignupResponse(pending.provider(), pending.provider().getLabel(), pending.email(), pending.name(),
                pending.nickname(), pending.mobilePhoneNumber(), suggestUsername(pending.provider()));
    }

    @Transactional
    public LoggedIn signup(final SocialJoinRequest request, final HttpSession session) {
        PendingSignup pending = requirePending(session);
        if (accountRepository.findByProviderAndProviderUserId(pending.provider(), pending.providerUserId()).isPresent()) {
            throw new BusinessException(ErrorCode.SOCIAL_SIGNUP_EXPIRED);
        }
        // 아무도 모르는 임의 비밀번호. BCrypt 가 72바이트까지만 받으므로 43자 하나만 쓴다.
        Long memberId = memberService.join(request.toJoinRequest(randomToken()));
        accountRepository.save(SocialAccount.builder()
                .memberId(memberId)
                .provider(pending.provider())
                .providerUserId(pending.providerUserId())
                .build());
        session.removeAttribute(PENDING_SIGNUP);
        return new LoggedIn(authService.authenticateSocialMember(memberId), pending.redirect());
    }

    private PendingSignup requirePending(final HttpSession session) {
        if (session != null && session.getAttribute(PENDING_SIGNUP) instanceof PendingSignup pending
                && pending.createdAt().plus(SIGNUP_TTL).isAfter(Instant.now())) {
            return pending;
        }
        throw new BusinessException(ErrorCode.SOCIAL_SIGNUP_EXPIRED);
    }

    // JDBC 세션은 setAttribute 때만 저장하므로 복사본을 고쳐 다시 넣는다.
    @SuppressWarnings("unchecked")
    private static LinkedHashMap<String, Authorization> authorizations(final HttpSession session) {
        return session.getAttribute(AUTHORIZATIONS) instanceof LinkedHashMap<?, ?> stored
                ? new LinkedHashMap<>((Map<String, Authorization>) stored)
                : new LinkedHashMap<>();
    }

    private static void saveAuthorization(final HttpSession session, final String state, final Authorization authorization) {
        LinkedHashMap<String, Authorization> map = authorizations(session);
        while (map.size() >= MAX_AUTHORIZATIONS) {
            map.remove(map.keySet().iterator().next());
        }
        map.put(state, authorization);
        session.setAttribute(AUTHORIZATIONS, map);
    }

    private static Authorization takeAuthorization(final HttpSession session, final String state) {
        LinkedHashMap<String, Authorization> map = authorizations(session);
        Authorization authorization = map.remove(state);
        session.setAttribute(AUTHORIZATIONS, map);
        return authorization;
    }

    private String suggestUsername(final SocialProvider provider) {
        String candidate;
        do {
            candidate = provider.path() + "_" + Long.toString(Math.abs(RANDOM.nextLong()), 36).substring(0, 6).toLowerCase(Locale.ROOT);
        } while (memberRepository.existsByUsername(candidate));
        return candidate;
    }

    static String safeRedirect(final String redirect) {
        if (redirect == null || !redirect.startsWith("/") || redirect.startsWith("//") || redirect.startsWith("/\\")) {
            return "/";
        }
        return redirect;
    }

    private static String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String s256(final String verifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
