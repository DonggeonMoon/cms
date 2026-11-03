package com.dgmoonlabs.cms.domain.common.policy;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PolicyApiTest extends ApiTestSupport {
    @Test
    void templateIsServedUntilTheAdminWritesTheText() throws Exception {
        getJson("/api/policies/PRIVACY_POLICY", null)
                .andExpect(jsonPath("$.title").value("개인정보처리방침"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.contentHtml").value(containsString("[작성 필요]")));

        MockHttpSession admin = admin();
        putJson("/api/admin/policies/PRIVACY_POLICY", admin, """
                {"contentHtml":"<h3>제1조</h3><p>우리 기관 방침</p><script>alert(1)</script>"}
                """).andExpect(jsonPath("$.version").value(1));

        getJson("/api/policies/PRIVACY_POLICY", null)
                .andExpect(jsonPath("$.contentHtml").value(containsString("우리 기관 방침")))
                .andExpect(jsonPath("$.contentHtml").value(not(containsString("<script"))))
                .andExpect(jsonPath("$.lastWorker").value("root"));
        getJson("/api/policies/PRIVACY_CONSENT", null).andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void onlyAdminsEditPolicies() throws Exception {
        putJson("/api/admin/policies/PRIVACY_CONSENT", member("policyuser"), "{\"contentHtml\":\"<p>x</p>\"}")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void joiningRequiresPrivacyConsentAndRecordsIt() throws Exception {
        postJson("/api/members", null, """
                {"username":"noconsent","password":"password123","passwordCheck":"password123","name":"미동의"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PRIVACY_CONSENT_REQUIRED"));

        MockHttpSession member = member("consented");
        getJson("/api/members/me", member).andExpect(status().isOk());
    }

    @Test
    void optionalConsentTextsCanBeSwitchedOffAndThenAreNeverRecorded() throws Exception {
        getJson("/api/policies/MARKETING_USAGE", null)
                .andExpect(jsonPath("$.title").value("마케팅 활용 동의"))
                .andExpect(jsonPath("$.isUsing").value(true))
                .andExpect(jsonPath("$.optional").value(true));

        MockHttpSession admin = admin();
        putJson("/api/admin/policies/MARKETING_USAGE", admin, """
                {"contentHtml":"<p>혜택 안내</p>","isUsing":false}
                """).andExpect(jsonPath("$.isUsing").value(false));
        putJson("/api/admin/policies/PRIVACY_CONSENT", admin, """
                {"contentHtml":"<p>필수 동의</p>","isUsing":false}
                """).andExpect(jsonPath("$.isUsing").value(true));

        postJson("/api/members", null, """
                {"username":"optout","password":"password123","passwordCheck":"password123","isPrivacyConsentAgreed":true,
                 "name":"선택","isMarketingUsageAgreed":true,"isSmsReceptionAgreed":true}
                """).andExpect(status().isCreated());
        getJson("/api/members/me", login("optout", "password123", "MEMBER"))
                .andExpect(jsonPath("$.isMarketingUsageAgreed").value(false))
                .andExpect(jsonPath("$.isSmsReceptionAgreed").value(true));
    }
}
