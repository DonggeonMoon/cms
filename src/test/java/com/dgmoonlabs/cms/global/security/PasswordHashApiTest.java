package com.dgmoonlabs.cms.global.security;

import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PasswordHashApiTest extends ApiTestSupport {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void newPasswordsUseTheChosenAlgorithmAndOldOnesAreRehashedOnLogin() throws Exception {
        MockHttpSession admin = admin();
        getJson("/api/admin/password-hash", admin).andExpect(jsonPath("$.code").value("REAUTHENTICATION_REQUIRED"));
        postJson("/api/admin/reauthenticate", admin, "{\"password\":\"adminpass1\"}").andExpect(status().isNoContent());
        getJson("/api/admin/password-hash", admin)
                .andExpect(jsonPath("$.algorithm").value("BCRYPT"))
                .andExpect(jsonPath("$.options.length()").value(4));

        member("hashold");
        assertThat(passwordOf("hashold")).startsWith("{bcrypt}");

        putJson("/api/admin/password-hash", admin, "{\"algorithm\":\"ARGON2\"}")
                .andExpect(jsonPath("$.algorithm").value("ARGON2"));
        member("hashnew");
        assertThat(passwordOf("hashnew")).startsWith("{argon2@SpringSecurity_v5_8}");

        login("hashold", "password123", "MEMBER");
        assertThat(passwordOf("hashold")).startsWith("{argon2@SpringSecurity_v5_8}");
        putJson("/api/admin/password-hash", admin, "{\"algorithm\":\"MD5\"}").andExpect(status().isBadRequest());
    }

    private String passwordOf(final String username) {
        return memberRepository.findByUsernameAndDeletedAtIsNull(username).orElseThrow().getPassword();
    }
}
