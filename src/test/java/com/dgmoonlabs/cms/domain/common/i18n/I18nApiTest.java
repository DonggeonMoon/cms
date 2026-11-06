package com.dgmoonlabs.cms.domain.common.i18n;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class I18nApiTest extends ApiTestSupport {
    @Test
    void languagesAreChosenAndTranslationsAreAddedOneAtATime() throws Exception {
        MockHttpSession admin = admin();
        getJson("/api/i18n", null).andExpect(jsonPath("$.defaultLanguage").value("ko"));
        putJson("/api/admin/i18n", admin, "{\"languages\":[\"ko\",\"en\"],\"defaultLanguage\":\"ja\"}")
                .andExpect(jsonPath("$.code").value("DEFAULT_LANGUAGE_NOT_SELECTED"));
        putJson("/api/admin/i18n", admin, "{\"languages\":[\"ko\",\"en\"],\"defaultLanguage\":\"ko\"}")
                .andExpect(jsonPath("$.languages[1]").value("en"));

        long loginId = createdId(postJson("/api/admin/i18n/translations/en", admin,
                "{\"key\":\"nav.login\",\"message\":\"Log in\"}"));
        postJson("/api/admin/i18n/translations/en", admin, "{\"key\":\"menu.3\",\"message\":\"Notice\"}");
        getJson("/api/i18n/messages/en", null)
                .andExpect(jsonPath("$['nav.login']").value("Log in"))
                .andExpect(jsonPath("$['menu.3']").value("Notice"));

        postJson("/api/admin/i18n/translations/en", admin, "{\"key\":\"nav.login\",\"message\":\"duplicate\"}")
                .andExpect(jsonPath("$.code").value("DUPLICATE_TRANSLATION_KEY"));

        putJson("/api/admin/i18n/translations/" + loginId, admin, "{\"key\":\"nav.login\",\"message\":\"Sign in\"}")
                .andExpect(jsonPath("$.message").value("Sign in"));
        getJson("/api/admin/i18n/translations/en", admin)
                .andExpect(jsonPath("$[0].message").value("Notice"))
                .andExpect(jsonPath("$[1].message").value("Sign in"));

        deleteJson("/api/admin/i18n/translations/" + loginId, admin, null);
        getJson("/api/i18n/messages/en", null).andExpect(jsonPath("$['nav.login']").doesNotExist());

        postJson("/api/admin/i18n/translations/en", admin, "{\"key\":\"bad key!\",\"message\":\"x\"}")
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }
}
