package com.dgmoonlabs.cms.domain.common.user;

import com.dgmoonlabs.cms.domain.common.user.entity.admin.entity.Admin;
import com.dgmoonlabs.cms.domain.common.user.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// the visitor site and the admin console keep separate logins, even within one browser session
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginZoneTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void adminLoginDoesNotSignInTheVisitorSite() throws Exception {
        adminRepository.save(Admin.builder().username("zoneadmin").password(passwordEncoder.encode("adminpass1")).name("관리자").build());
        MockHttpSession session = new MockHttpSession();
        login(session, "zoneadmin", "adminpass1", "ADMIN");

        mockMvc.perform(get("/api/auth/me").session(session).header("X-CMS-Zone", "admin"))
                .andExpect(jsonPath("$.userType").value("ADMIN"));
        mockMvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/members").session(session)).andExpect(status().isOk());
    }

    @Test
    void bothLoginsCoexistAndLogOutIndependently() throws Exception {
        adminRepository.save(Admin.builder().username("zoneadmin").password(passwordEncoder.encode("adminpass1")).name("관리자").build());
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                        {"username":"zonemember","password":"password123","passwordCheck":"password123","name":"회원",
                         "isPrivacyConsentAgreed":true}
                        """))
                .andExpect(status().isCreated());

        MockHttpSession session = new MockHttpSession();
        login(session, "zonemember", "password123", "MEMBER");
        login(session, "zoneadmin", "adminpass1", "ADMIN");

        mockMvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.username").value("zonemember"));
        mockMvc.perform(get("/api/auth/me").session(session).header("X-CMS-Zone", "admin"))
                .andExpect(jsonPath("$.username").value("zoneadmin"));

        mockMvc.perform(post("/api/auth/logout").session(session).header("X-CMS-Zone", "admin").with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/auth/me").session(session).header("X-CMS-Zone", "admin")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/members/me").session(session)).andExpect(jsonPath("$.username").value("zonemember"));
    }

    @Test
    void zoneHeaderCannotGrantAdminRightsToAMember() throws Exception {
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("""
                        {"username":"sneaky","password":"password123","passwordCheck":"password123","name":"회원",
                         "isPrivacyConsentAgreed":true}
                        """))
                .andExpect(status().isCreated());
        MockHttpSession session = new MockHttpSession();
        login(session, "sneaky", "password123", "MEMBER");

        mockMvc.perform(get("/api/admin/members").session(session).header("X-CMS-Zone", "admin"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/members").session(session)).andExpect(status().isUnauthorized());
    }

    private void login(final MockHttpSession session, final String username, final String password, final String type)
            throws Exception {
        mockMvc.perform(post("/api/auth/login").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\",\"userType\":\"%s\"}".formatted(username, password, type)))
                .andExpect(status().isOk());
    }
}
