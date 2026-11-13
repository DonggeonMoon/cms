package com.dgmoonlabs.cms.domain.common.identity;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IdentityVerificationApiTest extends ApiTestSupport {
    @Test
    void isDisabledByDefaultAndFallsBackToTheNoopProviderOnceEnabled() throws Exception {
        getJson("/api/identity-verification", null).andExpect(jsonPath("$.enabled").value(false));
        postJson("/api/identity-verifications/start", null, "{\"returnUrl\":\"https://example.com/done\"}")
                .andExpect(jsonPath("$.code").value("IDENTITY_VERIFICATION_DISABLED"));

        MockHttpSession admin = admin();
        putJson("/api/admin/identity-verification", admin, "{\"enabled\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
        getJson("/api/identity-verification", null).andExpect(jsonPath("$.enabled").value(true));

        postJson("/api/identity-verifications/start", null, "{\"returnUrl\":\"https://example.com/done\"}")
                .andExpect(jsonPath("$.code").value("IDENTITY_VERIFICATION_NOT_CONFIGURED"));
    }
}
