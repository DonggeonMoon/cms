package com.dgmoonlabs.cms.domain.admin.crypto;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CryptoKeyApiTest extends ApiTestSupport {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void theFirstAdminLoginCreatesAnAesKeyAndPersonalDataIsStoredEncrypted() throws Exception {
        MockHttpSession admin = admin();
        JsonNode key = body(getJson("/api/admin/crypto-key", admin).andExpect(status().isOk()));
        assertThat(key.get("initialized").asBoolean()).isTrue();
        assertThat(key.get("algorithm").asString()).isEqualTo("AES");
        assertThat(key.get("key").asString()).matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[0-9a-f]{4}-[0-9a-f]{12}");

        MockHttpSession member = member("cryptoone");
        assertThat(rawName("cryptoone")).startsWith("ENC:AES:");
        assertThat(rawUsername("cryptoone")).isEqualTo("cryptoone");
        getJson("/api/members/me", member).andExpect(jsonPath("$.name").value("cryptoone님"));
        getJson("/api/admin/members?keyword=cryptoone", admin).andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void regeneratingTheKeyReencryptsEverythingUnderTheNewOne() throws Exception {
        MockHttpSession admin = admin();
        MockHttpSession member = member("cryptotwo");
        String before = rawName("cryptotwo");
        String oldKey = body(getJson("/api/admin/crypto-key", admin)).get("key").asString();

        JsonNode result = body(postJson("/api/admin/crypto-key/regenerate", admin, "").andExpect(status().isOk()));
        assertThat(result.get("key").get("key").asString()).isNotEqualTo(oldKey);
        assertThat(result.get("reencryptedValues").asInt()).isPositive();

        String after = rawName("cryptotwo");
        assertThat(after).startsWith("ENC:AES:").isNotEqualTo(before);
        assertThat(keyIdOf(after)).isNotEqualTo(keyIdOf(before));
        getJson("/api/members/me", member).andExpect(jsonPath("$.name").value("cryptotwo님"));
    }

    @Test
    void changingTheAlgorithmReencryptsWithSeedOrAria() throws Exception {
        MockHttpSession admin = admin();
        MockHttpSession member = member("cryptothree");

        putJson("/api/admin/crypto-key/algorithm", admin, """
                {"algorithm":"SEED"}
                """).andExpect(jsonPath("$.key.algorithm").value("SEED"));
        assertThat(rawName("cryptothree")).startsWith("ENC:SEED:");
        getJson("/api/members/me", member).andExpect(jsonPath("$.name").value("cryptothree님"));

        putJson("/api/admin/crypto-key/algorithm", admin, """
                {"algorithm":"ARIA"}
                """).andExpect(jsonPath("$.key.algorithm").value("ARIA"));
        assertThat(rawName("cryptothree")).startsWith("ENC:ARIA:");
        getJson("/api/members/me", member).andExpect(jsonPath("$.name").value("cryptothree님"));
    }

    @Test
    void onlyAdminsSeeTheKey() throws Exception {
        admin();
        getJson("/api/admin/crypto-key", member("cryptofour")).andExpect(status().isUnauthorized());
        getJson("/api/admin/crypto-key", null).andExpect(status().isUnauthorized());
    }

    private String rawName(final String username) {
        return jdbcTemplate.queryForObject("select name from member where username = ?", String.class, username);
    }

    private String rawUsername(final String username) {
        return jdbcTemplate.queryForObject("select username from member where username = ?", String.class, username);
    }

    private static String keyIdOf(final String sealed) {
        return sealed.split(":")[2];
    }
}
