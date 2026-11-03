package com.dgmoonlabs.cms.domain.common.social;

import com.dgmoonlabs.cms.domain.common.social.client.SocialOAuthClient;
import com.dgmoonlabs.cms.domain.common.social.client.SocialProfile;
import com.dgmoonlabs.cms.domain.common.social.constant.SocialProvider;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SocialLoginApiTest extends ApiTestSupport {
    @MockitoBean
    private SocialOAuthClient oauthClient;

    @Test
    void firstLoginSignsUpThenLaterLoginsGoStraightIn() throws Exception {
        enableKakao();
        getJson("/api/auth/oauth/providers", null)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].path").value("kakao"));
        given(oauthClient.fetchProfile(eq(SocialProvider.KAKAO), any(), eq("code-1"), any(), any(), any(), any(), any()))
                .willReturn(new SocialProfile("kakao-42", "sky@example.com", "김하늘", "하늘", null));

        MockHttpSession browser = new MockHttpSession();
        String state = authorize(browser, "/media");
        mockMvc.perform(get("/api/auth/oauth/kakao/callback").param("code", "code-1").param("state", state).session(browser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/join/social"));
        getJson("/api/auth/oauth/signup", browser)
                .andExpect(jsonPath("$.provider").value("KAKAO"))
                .andExpect(jsonPath("$.email").value("sky@example.com"))
                .andExpect(jsonPath("$.suggestedUsername").value(org.hamcrest.Matchers.startsWith("kakao_")));
        postJson("/api/auth/oauth/signup", browser, """
                {"username":"kakaosky","name":"김하늘","isPrivacyConsentAgreed":true}
                """).andExpect(status().isOk()).andExpect(jsonPath("$.redirect").value("/media"));
        getJson("/api/auth/me", browser).andExpect(jsonPath("$.username").value("kakaosky"));

        MockHttpSession again = new MockHttpSession();
        String secondState = authorize(again, "/mypage");
        mockMvc.perform(get("/api/auth/oauth/kakao/callback").param("code", "code-1").param("state", secondState).session(again))
                .andExpect(redirectedUrl("/mypage"));
        getJson("/api/auth/me", again).andExpect(jsonPath("$.username").value("kakaosky"));
    }

    @Test
    void callbacksNeedTheStateOfThisBrowserAndStayOnSite() throws Exception {
        enableKakao();
        MockHttpSession browser = new MockHttpSession();
        authorize(browser, "//evil.example.com");
        mockMvc.perform(get("/api/auth/oauth/kakao/callback").param("code", "x").param("state", "forged").session(browser))
                .andExpect(redirectedUrl("/login?socialError=state"));

        given(oauthClient.fetchProfile(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(new SocialProfile("kakao-7", null, null, null, null));
        String state = authorize(browser, "//evil.example.com");
        mockMvc.perform(get("/api/auth/oauth/kakao/callback").param("code", "x").param("state", state).session(browser))
                .andExpect(redirectedUrl("/join/social"));
        postJson("/api/auth/oauth/signup", browser, """
                {"username":"kakaoseven","name":"칠","isPrivacyConsentAgreed":true}
                """).andExpect(jsonPath("$.redirect").value("/"));

        getJson("/api/auth/oauth/naver/authorize", null).andExpect(jsonPath("$.code").value("SOCIAL_LOGIN_DISABLED"));
        mockMvc.perform(post("/api/auth/oauth/apple/callback").contentType("application/x-www-form-urlencoded")
                        .param("code", "c").param("state", "s"))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/api/auth/oauth/apple/callback?code=c&state=s"));
    }

    @Test
    void settingsNeedReauthenticationAndAllKeysBeforeEnabling() throws Exception {
        MockHttpSession admin = admin();
        getJson("/api/admin/social-logins", admin).andExpect(jsonPath("$.code").value("REAUTHENTICATION_REQUIRED"));
        verify(admin);
        long clientId = createdId(postJson("/api/admin/api-keys", admin, "{\"name\":\"네이버 ID\",\"value\":\"naver-id\"}"));
        putJson("/api/admin/social-logins/naver", admin, "{\"enabled\":true,\"clientIdKeyId\":" + clientId + "}")
                .andExpect(jsonPath("$.code").value("SOCIAL_LOGIN_NOT_CONFIGURED"));
        getJson("/api/admin/social-logins", admin)
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[?(@.provider == 'APPLE')].redirectUri").value("http://localhost/api/auth/oauth/apple/callback"));
    }

    private void enableKakao() throws Exception {
        MockHttpSession admin = verify(admin());
        long clientId = createdId(postJson("/api/admin/api-keys", admin, "{\"name\":\"카카오 REST 키\",\"value\":\"kakao-client\"}"));
        putJson("/api/admin/social-logins/kakao", admin, "{\"enabled\":true,\"clientIdKeyId\":" + clientId + "}")
                .andExpect(jsonPath("$.enabled").value(true));
    }

    private String authorize(final MockHttpSession browser, final String redirect) throws Exception {
        String location = mockMvc.perform(get("/api/auth/oauth/kakao/authorize").param("redirect", redirect).session(browser))
                .andExpect(status().isFound())
                .andReturn().getResponse().getRedirectedUrl();
        assertThat(location).startsWith("https://kauth.kakao.com/oauth/authorize?")
                .contains("client_id=kakao-client")
                .contains("redirect_uri=http://localhost/api/auth/oauth/kakao/callback");
        return UriComponentsBuilder.fromUriString(location).build().getQueryParams().getFirst("state");
    }

    private MockHttpSession verify(final MockHttpSession admin) throws Exception {
        postJson("/api/admin/reauthenticate", admin, "{\"password\":\"adminpass1\"}").andExpect(status().isNoContent());
        return admin;
    }
}
