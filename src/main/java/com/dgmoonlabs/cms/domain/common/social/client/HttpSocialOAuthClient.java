package com.dgmoonlabs.cms.domain.common.social.client;

import com.dgmoonlabs.cms.domain.common.social.constant.SocialProvider;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class HttpSocialOAuthClient implements SocialOAuthClient {
    private static final String GRAPH = "https://graph.facebook.com/" + SocialProvider.FACEBOOK_GRAPH_VERSION;

    private final RestClient restClient;
    private final JsonMapper jsonMapper;

    public HttpSocialOAuthClient(final JsonMapper jsonMapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.jsonMapper = jsonMapper;
    }

    @Override
    public SocialProfile fetchProfile(final SocialProvider provider, final SocialCredentials credentials, final String code,
                                      final String redirectUri, final String state, final String codeVerifier,
                                      final String nonce, final String appleUser) {
        try {
            return switch (provider) {
                case KAKAO -> kakao(credentials, code, redirectUri);
                case NAVER -> naver(credentials, code, state);
                case GOOGLE -> google(credentials, code, redirectUri, codeVerifier);
                case APPLE -> apple(credentials, code, redirectUri, nonce, appleUser);
                case FACEBOOK -> facebook(credentials, code, redirectUri);
            };
        } catch (RestClientResponseException e) {
            log.warn("Social login {} answered {}: {}", provider, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, e);
        } catch (RestClientException | JwtException | IllegalArgumentException e) {
            log.warn("Social login {} failed", provider, e);
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, e);
        }
    }

    private SocialProfile kakao(final SocialCredentials credentials, final String code, final String redirectUri) {
        MultiValueMap<String, String> form = form("authorization_code", credentials.clientId(), code, redirectUri);
        if (credentials.clientSecret() != null) {
            form.add("client_secret", credentials.clientSecret());
        }
        String accessToken = token("https://kauth.kakao.com/oauth/token", form);
        JsonNode me = get("https://kapi.kakao.com/v2/user/me", accessToken);
        JsonNode account = me.path("kakao_account");
        return new SocialProfile(me.path("id").asString(), text(account, "email"), text(account, "name"),
                text(account.path("profile"), "nickname"), null);
    }

    private SocialProfile naver(final SocialCredentials credentials, final String code, final String state) {
        MultiValueMap<String, String> form = form("authorization_code", credentials.clientId(), code, null);
        form.add("client_secret", credentials.clientSecret());
        form.add("state", state);
        String accessToken = token("https://nid.naver.com/oauth2.0/token", form);
        JsonNode me = get("https://openapi.naver.com/v1/nid/me", accessToken).path("response");
        return new SocialProfile(me.path("id").asString(), text(me, "email"), text(me, "name"), text(me, "nickname"),
                text(me, "mobile"));
    }

    private SocialProfile google(final SocialCredentials credentials, final String code, final String redirectUri,
                                 final String codeVerifier) {
        MultiValueMap<String, String> form = form("authorization_code", credentials.clientId(), code, redirectUri);
        form.add("client_secret", credentials.clientSecret());
        if (codeVerifier != null) {
            form.add("code_verifier", codeVerifier);
        }
        String accessToken = token("https://oauth2.googleapis.com/token", form);
        JsonNode me = get("https://openidconnect.googleapis.com/v1/userinfo", accessToken);
        String email = me.path("email_verified").asBoolean(false) ? text(me, "email") : null;
        return new SocialProfile(me.path("sub").asString(), email, text(me, "name"), null, null);
    }

    private SocialProfile facebook(final SocialCredentials credentials, final String code, final String redirectUri) {
        JsonNode token = restClient.get()
                .uri(GRAPH + "/oauth/access_token?client_id={id}&client_secret={secret}&redirect_uri={redirect}&code={code}",
                        credentials.clientId(), credentials.clientSecret(), redirectUri, code)
                .retrieve().body(JsonNode.class);
        JsonNode me = get(GRAPH + "/me?fields=id,name,email", requireText(token, "access_token"));
        return new SocialProfile(me.path("id").asString(), text(me, "email"), text(me, "name"), null, null);
    }

    private SocialProfile apple(final SocialCredentials credentials, final String code, final String redirectUri,
                                final String nonce, final String appleUser) {
        String clientSecret = AppleSignIn.clientSecret(credentials.appleTeamId(), credentials.appleKeyId(),
                credentials.clientId(), credentials.applePrivateKey(), Instant.now());
        MultiValueMap<String, String> form = form("authorization_code", credentials.clientId(), code, redirectUri);
        form.add("client_secret", clientSecret);
        JsonNode token = restClient.post().uri("https://appleid.apple.com/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                .retrieve().body(JsonNode.class);
        Jwt idToken = AppleSignIn.verify(AppleSignIn.JWK_SET_URI, requireText(token, "id_token"), credentials.clientId(), nonce);
        String name = null;
        if (appleUser != null && !appleUser.isBlank()) {
            JsonNode user = jsonMapper.readTree(appleUser).path("name");
            String full = (user.path("lastName").asString("") + user.path("firstName").asString("")).trim();
            name = full.isEmpty() ? null : full;
        }
        return new SocialProfile(idToken.getSubject(), idToken.getClaimAsString("email"), name, null, null);
    }

    private static MultiValueMap<String, String> form(final String grantType, final String clientId, final String code,
                                                      final String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", grantType);
        form.add("client_id", clientId);
        form.add("code", code);
        if (redirectUri != null) {
            form.add("redirect_uri", redirectUri);
        }
        return form;
    }

    private String token(final String uri, final MultiValueMap<String, String> form) {
        JsonNode body = restClient.post().uri(uri).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
                .retrieve().body(JsonNode.class);
        return requireText(body, "access_token");
    }

    private JsonNode get(final String uri, final String accessToken) {
        return restClient.get().uri(uri).header("Authorization", "Bearer " + accessToken).retrieve().body(JsonNode.class);
    }

    private static String requireText(final JsonNode node, final String field) {
        String value = node == null ? null : text(node, field);
        if (value == null) {
            throw new IllegalArgumentException("응답에 " + field + " 값이 없습니다.");
        }
        return value;
    }

    private static String text(final JsonNode node, final String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || value.asString("").isBlank() ? null : value.asString();
    }
}
