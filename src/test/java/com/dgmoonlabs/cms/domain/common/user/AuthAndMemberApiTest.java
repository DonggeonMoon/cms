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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthAndMemberApiTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final String JOIN_BODY = """
            {"username":"tester","password":"password123","passwordCheck":"password123","isPrivacyConsentAgreed":true,"name":"테스터",
             "email":"tester@example.com","postalCode":"12345","address":"서울","detailAddress":"101호",
             "isMailReceptionAgreed":true,"officeName":"DG Labs"}
            """;

    @Test
    void joinLoginAndManageOwnAccount() throws Exception {
        join();
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(JOIN_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_USERNAME"));
        mockMvc.perform(get("/api/members/username-availability").param("username", "tester"))
                .andExpect(jsonPath("$.available").value(false));

        MockHttpSession session = login("tester", "password123", "MEMBER");

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.userType").value("MEMBER"));

        mockMvc.perform(get("/api/members/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("서울"))
                .andExpect(jsonPath("$.isMailReceptionAgreed").value(true))
                .andExpect(jsonPath("$.officeName").value("DG Labs"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(put("/api/members/me").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"새이름","email":"new@example.com","address":"부산","isSmsReceptionAgreed":true}
                                """))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/members/me").session(session))
                .andExpect(jsonPath("$.name").value("새이름"))
                .andExpect(jsonPath("$.address").value("부산"))
                .andExpect(jsonPath("$.isSmsReceptionAgreed").value(true));

        mockMvc.perform(put("/api/members/me/password").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"wrong-pass","newPassword":"newpassword1","newPasswordCheck":"newpassword1"}
                                """))
                .andExpect(jsonPath("$.code").value("WRONG_PASSWORD"));
        mockMvc.perform(put("/api/members/me/password").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"password123","newPassword":"newpassword1","newPasswordCheck":"newpassword1"}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/members/me").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

        login("tester", "newpassword1", "MEMBER");
    }

    @Test
    void rejectsInvalidJoinInput() throws Exception {
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"A!","password":"short","passwordCheck":"short","name":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(3));
    }

    @Test
    void locksAccountAfterRepeatedFailures() throws Exception {
        join();
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(loginRequest("tester", "bad-password", "MEMBER"))
                    .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
        }
        mockMvc.perform(loginRequest("tester", "password123", "MEMBER"))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
    }

    @Test
    void quitMemberCannotLogInAgain() throws Exception {
        join();
        MockHttpSession session = login("tester", "password123", "MEMBER");
        mockMvc.perform(delete("/api/members/me").session(session).with(csrf())).andExpect(status().isNoContent());
        mockMvc.perform(loginRequest("tester", "password123", "MEMBER")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminApisRequireAdminRole() throws Exception {
        join();
        adminRepository.save(Admin.builder().username("root").password(passwordEncoder.encode("adminpass1")).name("관리자").build());

        MockHttpSession member = login("tester", "password123", "MEMBER");
        // a member login lives in the site zone only, so the admin console sees nobody signed in
        mockMvc.perform(get("/api/admin/members").session(member))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        mockMvc.perform(get("/api/admin/members")).andExpect(status().isUnauthorized());

        MockHttpSession admin = login("root", "adminpass1", "ADMIN");
        mockMvc.perform(get("/api/admin/members").session(admin).param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("tester"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
        mockMvc.perform(get("/api/members/me").session(admin)).andExpect(status().isUnauthorized());
    }

    private void join() throws Exception {
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(JOIN_BODY))
                .andExpect(status().isCreated());
    }

    private MockHttpSession login(final String username, final String password, final String userType) throws Exception {
        MvcResult result = mockMvc.perform(loginRequest(username, password, userType))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private org.springframework.test.web.servlet.RequestBuilder loginRequest(final String username, final String password,
                                                                            final String userType) {
        return post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"password\":\"%s\",\"userType\":\"%s\"}".formatted(username, password, userType));
    }
}
