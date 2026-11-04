package com.dgmoonlabs.cms.domain.common.i18n;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class I18nApiTest extends ApiTestSupport {
    @Test
    void languagesAreChosenAndBundlesAcceptPropertiesAndYaml() throws Exception {
        MockHttpSession admin = admin();
        getJson("/api/i18n", null).andExpect(jsonPath("$.defaultLanguage").value("ko"));
        putJson("/api/admin/i18n", admin, "{\"languages\":[\"ko\",\"en\"],\"defaultLanguage\":\"ja\"}")
                .andExpect(jsonPath("$.code").value("DEFAULT_LANGUAGE_NOT_SELECTED"));
        putJson("/api/admin/i18n", admin, "{\"languages\":[\"ko\",\"en\"],\"defaultLanguage\":\"ko\"}")
                .andExpect(jsonPath("$.languages[1]").value("en"));

        putJson("/api/admin/i18n/bundles/en", admin, """
                {"format":"PROPERTIES","text":"nav.login = Log in\\nnav.mypage=My page\\n# comment\\nmenu.3 = Notice"}
                """).andExpect(jsonPath("$.count").value(3));
        getJson("/api/i18n/messages/en", null)
                .andExpect(jsonPath("$['nav.login']").value("Log in"))
                .andExpect(jsonPath("$['menu.3']").value("Notice"));

        putJson("/api/admin/i18n/bundles/en", admin, """
                {"format":"YAML","text":"nav:\\n  login: Sign in\\n  logout: Sign out\\nsite.name: Junggo Heaven"}
                """).andExpect(jsonPath("$.count").value(3));
        getJson("/api/i18n/messages/en", null)
                .andExpect(jsonPath("$['nav.login']").value("Sign in"))
                .andExpect(jsonPath("$['site.name']").value("Junggo Heaven"))
                .andExpect(jsonPath("$['menu.3']").doesNotExist());
        String yaml = body(getJson("/api/admin/i18n/bundles/en?format=YAML", admin)).get("text").asString();
        assertThat(yaml).contains("nav.login: Sign in");

        putJson("/api/admin/i18n/bundles/en", admin, "{\"format\":\"YAML\",\"text\":\"- just\\n- a list\"}")
                .andExpect(jsonPath("$.code").value("INVALID_TRANSLATION_BUNDLE"));
    }
}
