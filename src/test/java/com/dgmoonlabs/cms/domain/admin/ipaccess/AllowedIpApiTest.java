package com.dgmoonlabs.cms.domain.admin.ipaccess;

import com.dgmoonlabs.cms.domain.admin.ipaccess.service.IpRange;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AllowedIpApiTest extends ApiTestSupport {
    private void reauthenticate(final MockHttpSession admin) throws Exception {
        postJson("/api/admin/reauthenticate", admin, "{\"password\":\"adminpass1\"}").andExpect(status().isNoContent());
    }

    private ResultActions adminGetFrom(final String ip, final MockHttpSession admin) throws Exception {
        return mockMvc.perform(get("/api/admin/dashboard").session(admin).header("X-CMS-Zone", "admin")
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                }));
    }

    private ResultActions adminLoginFrom(final String ip, final String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"root\",\"password\":\"%s\",\"userType\":\"ADMIN\"}".formatted(password))
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                }));
    }

    @Test
    void parsesAddressesAndRanges() {
        assertThat(IpRange.parse("203.0.113.9/24").canonical()).isEqualTo("203.0.113.0/24");
        assertThat(IpRange.parse("203.0.113.0/24").contains("203.0.113.250")).isTrue();
        assertThat(IpRange.parse("203.0.113.0/24").contains("203.0.114.1")).isFalse();
        assertThat(IpRange.parse("2001:db8::/32").contains("2001:db8:1::5")).isTrue();
        assertThat(IpRange.parse("10.0.0.1").contains("0:0:0:0:0:0:0:1")).isFalse();
        assertThat(IpRange.parse("10.0.0.0/8").contains("not-an-ip")).isFalse();
    }

    @Test
    void onlyListedAddressesReachAdminAfterTurningOn() throws Exception {
        MockHttpSession admin = admin();
        putJson("/api/admin/allowed-ips/enabled", admin, "{\"enabled\":true}").andExpect(jsonPath("$.code").value("REAUTHENTICATION_REQUIRED"));
        reauthenticate(admin);

        putJson("/api/admin/allowed-ips/enabled", admin, "{\"enabled\":true}").andExpect(jsonPath("$.code").value("ADMIN_IP_LOCKOUT"));
        postJson("/api/admin/allowed-ips", admin, "{\"address\":\"example.com\"}").andExpect(jsonPath("$.code").value("INVALID_IP_ADDRESS"));
        postJson("/api/admin/allowed-ips", admin, "{\"address\":\"203.0.113.0/33\"}").andExpect(jsonPath("$.code").value("INVALID_IP_ADDRESS"));
        postJson("/api/admin/allowed-ips", admin, "{\"address\":\"127.0.0.1\",\"memo\":\"내 컴퓨터\"}")
                .andExpect(jsonPath("$.currentIpAllowed").value(true));
        long officeId = body(postJson("/api/admin/allowed-ips", admin, "{\"address\":\"203.0.113.40/24\",\"memo\":\"사무실\"}")
                .andExpect(jsonPath("$.entries[1].address").value("203.0.113.0/24"))).get("entries").get(1).get("id").asLong();
        postJson("/api/admin/allowed-ips", admin, "{\"address\":\"203.0.113.0/24\"}").andExpect(jsonPath("$.code").value("DUPLICATE_ALLOWED_IP"));

        adminGetFrom("198.51.100.7", admin).andExpect(status().isOk());
        putJson("/api/admin/allowed-ips/enabled", admin, "{\"enabled\":true}").andExpect(jsonPath("$.enabled").value(true));

        adminGetFrom("203.0.113.77", admin).andExpect(status().isOk());
        adminGetFrom("198.51.100.7", admin).andExpect(jsonPath("$.code").value("ADMIN_IP_NOT_ALLOWED"));
        adminLoginFrom("198.51.100.7", "wrong").andExpect(jsonPath("$.code").value("ADMIN_IP_NOT_ALLOWED"));
        adminLoginFrom("203.0.113.5", "adminpass1").andExpect(status().isOk());
        getJson("/api/products", null).andExpect(status().isOk());

        long selfId = body(getJson("/api/admin/allowed-ips", admin)).get("entries").get(0).get("id").asLong();
        deleteJson("/api/admin/allowed-ips/" + selfId, admin, null).andExpect(jsonPath("$.code").value("ADMIN_IP_LOCKOUT"));
        deleteJson("/api/admin/allowed-ips/" + officeId, admin, null).andExpect(jsonPath("$.entries.length()").value(1));
        adminGetFrom("203.0.113.77", admin).andExpect(jsonPath("$.code").value("ADMIN_IP_NOT_ALLOWED"));

        putJson("/api/admin/allowed-ips/enabled", admin, "{\"enabled\":false}").andExpect(jsonPath("$.enabled").value(false));
        adminGetFrom("198.51.100.7", admin).andExpect(status().isOk());
    }
}
