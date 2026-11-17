package com.dgmoonlabs.cms.domain.postcode;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostcodeApiTest extends ApiTestSupport {

    @Test
    void defaultsToKakaoWithoutAnApiKey() throws Exception {
        getJson("/api/postcode", null).andExpect(jsonPath("$.provider").value("KAKAO"));
    }

    @Test
    void jusoProviderRequiresAnApiKey() throws Exception {
        MockHttpSession admin = admin();
        putJson("/api/admin/postcode/settings", admin, """
                {"provider":"JUSO"}
                """).andExpect(jsonPath("$.code").value("POSTCODE_API_KEY_REQUIRED"));

        postJson("/api/admin/reauthenticate", admin, """
                {"password":"adminpass1"}
                """).andExpect(status().isNoContent());
        long jusoKey = createdId(postJson("/api/admin/api-keys", admin, """
                {"name":"행안부 도로명주소 키","value":"juso-key"}
                """));

        putJson("/api/admin/postcode/settings", admin, """
                {"provider":"JUSO","jusoApiKeyId":%d}
                """.formatted(jusoKey)).andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("JUSO"))
                .andExpect(jsonPath("$.jusoApiKeyId").value(jusoKey));

        getJson("/api/postcode", null).andExpect(jsonPath("$.provider").value("JUSO"));
        getJson("/api/admin/postcode/settings", admin).andExpect(jsonPath("$.jusoApiKeyId").value(jusoKey));

        putJson("/api/admin/postcode/settings", admin, """
                {"provider":"KAKAO"}
                """).andExpect(jsonPath("$.provider").value("KAKAO"));
        getJson("/api/postcode", null).andExpect(jsonPath("$.provider").value("KAKAO"));
    }
}
