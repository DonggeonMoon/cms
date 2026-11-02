package com.dgmoonlabs.cms.domain.admin.apikey;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiKeyApiTest extends ApiTestSupport {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void keysAreManagedByNameAndTheValueIsStoredEncrypted() throws Exception {
        MockHttpSession admin = verified(admin());
        long id = createdId(postJson("/api/admin/api-keys", admin, """
                {"name":"카카오맵 JS 키","value":"  kakao-secret  ","description":"운영 도메인용"}
                """).andExpect(status().isCreated()));

        getJson("/api/admin/api-keys/" + id, admin)
                .andExpect(jsonPath("$.name").value("카카오맵 JS 키"))
                .andExpect(jsonPath("$.value").value("kakao-secret"));
        getJson("/api/admin/api-keys/options", admin)
                .andExpect(jsonPath("$[0].name").value("카카오맵 JS 키"))
                .andExpect(jsonPath("$[0].value").doesNotExist());
        assertThat(jdbcTemplate.queryForObject("select \"value\" from api_key where id = ?", String.class, id))
                .startsWith("ENC:");

        postJson("/api/admin/api-keys", admin, """
                {"name":"카카오맵 JS 키","value":"other"}
                """).andExpect(jsonPath("$.code").value("DUPLICATE_API_KEY_NAME"));
        putJson("/api/admin/api-keys/" + id, admin, """
                {"name":"카카오맵 JS 키","value":"rotated"}
                """).andExpect(status().isNoContent());
        getJson("/api/admin/api-keys/" + id, admin).andExpect(jsonPath("$.value").value("rotated"));
    }

    @Test
    void aKeySelectedInTheMapSettingsCannotBeDeleted() throws Exception {
        MockHttpSession admin = verified(admin());
        long id = createdId(postJson("/api/admin/api-keys", admin, """
                {"name":"지도 키","value":"map-key"}
                """));
        putJson("/api/admin/map/settings", admin, """
                {"provider":"KAKAO","kakaoApiKeyId":%d}
                """.formatted(id)).andExpect(status().isOk());
        getJson("/api/map", null).andExpect(jsonPath("$.apiKey").value("map-key"));
        getJson("/api/admin/map/settings", admin).andExpect(jsonPath("$.apiKey").value("map-key"));

        deleteJson("/api/admin/api-keys/" + id, admin, null).andExpect(jsonPath("$.code").value("API_KEY_IN_USE"));

        putJson("/api/admin/map/settings", admin, """
                {"provider":"KAKAO"}
                """).andExpect(status().isOk());
        deleteJson("/api/admin/api-keys/" + id, admin, null).andExpect(status().isNoContent());
        getJson("/api/map", null).andExpect(jsonPath("$.apiKey").doesNotExist());
    }

    @Test
    void theMapRejectsAKeyThatIsNotRegistered() throws Exception {
        putJson("/api/admin/map/settings", admin(), """
                {"provider":"KAKAO","kakaoApiKeyId":999999}
                """).andExpect(jsonPath("$.code").value("API_KEY_NOT_FOUND"));
    }

    @Test
    void managingKeysNeedsThePasswordAgainButPickingOneDoesNot() throws Exception {
        MockHttpSession admin = admin();
        getJson("/api/admin/api-keys", admin).andExpect(jsonPath("$.code").value("REAUTHENTICATION_REQUIRED"));
        postJson("/api/admin/api-keys", admin, """
                {"name":"키","value":"v"}
                """).andExpect(status().isForbidden());
        getJson("/api/admin/api-keys/options", admin).andExpect(status().isOk());

        verified(admin);
        getJson("/api/admin/api-keys", admin).andExpect(status().isOk());
    }

    @Test
    void onlyAdminsManageKeys() throws Exception {
        getJson("/api/admin/api-keys", member("apikeyuser")).andExpect(status().isUnauthorized());
        getJson("/api/admin/api-keys", null).andExpect(status().isUnauthorized());
    }

    private MockHttpSession verified(final MockHttpSession admin) throws Exception {
        postJson("/api/admin/reauthenticate", admin, """
                {"password":"adminpass1"}
                """).andExpect(status().isNoContent());
        return admin;
    }
}
