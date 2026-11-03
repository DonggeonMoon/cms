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
                .andExpect(jsonPath("$.address").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.isMailReceptionAgreed").value(true))
                .andExpect(jsonPath("$.officeName").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(put("/api/members/me").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"새이름","mobilePhoneNumber":"010-1111-2222","address":"부산","isSmsReceptionAgreed":true}
                                """))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/members/me").session(session))
                .andExpect(jsonPath("$.name").value("새이름"))
                .andExpect(jsonPath("$.mobilePhoneNumber").value("010-1111-2222"))
                .andExpect(jsonPath("$.address").doesNotExist())
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
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(JOIN_BODY.replace("\"name\":\"테스터\"", "\"name\":\" \"")))
                .andExpect(jsonPath("$.code").value("MEMBER_NAME_REQUIRED"));
    }

    @Test
    void joinSettingsDecideNameFormatAndCollectedItems() throws Exception {
        mockMvc.perform(get("/api/members/join-settings"))
                .andExpect(jsonPath("$.nameFormat").value("FULL"))
                .andExpect(jsonPath("$.birthDateInput").value("SELECT"))
                .andExpect(jsonPath("$.collectedItems.length()").value(2));

        adminRepository.save(Admin.builder().username("root").password(passwordEncoder.encode("adminpass1")).name("관리자").build());
        MockHttpSession admin = login("root", "adminpass1", "ADMIN");
        mockMvc.perform(get("/api/admin/members/join-settings").session(admin))
                .andExpect(jsonPath("$.code").value("REAUTHENTICATION_REQUIRED"));
        mockMvc.perform(post("/api/admin/reauthenticate").session(admin).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"adminpass1\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/api/admin/members/join-settings").session(admin).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nameFormat":"SPLIT","birthDateInput":"SIX_DIGITS","collectedItems":["ADDRESS","GENDER"]}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/members/join-settings"))
                .andExpect(jsonPath("$.nameFormat").value("SPLIT"))
                .andExpect(jsonPath("$.birthDateInput").value("SIX_DIGITS"));

        String body = """
                {"username":"split","password":"password123","passwordCheck":"password123","isPrivacyConsentAgreed":true,
                 "lastName":"홍","firstName":"길동","gender":"남","birthDate":"1990-01-02","address":"서울"}
                """;
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("\"firstName\":\"길동\",", "")))
                .andExpect(jsonPath("$.code").value("MEMBER_NAME_REQUIRED"));
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        MockHttpSession member = login("split", "password123", "MEMBER");
        mockMvc.perform(get("/api/members/me").session(member))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.lastName").value("홍"))
                .andExpect(jsonPath("$.gender").value("남"))
                .andExpect(jsonPath("$.address").value("서울"))
                .andExpect(jsonPath("$.birthDate").doesNotExist());

        mockMvc.perform(put("/api/admin/members/join-settings").session(admin).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"collectName":false,"nameFormat":"FULL","collectNickname":true,"collectedItems":[]}
                                """))
                .andExpect(jsonPath("$.collectName").value(false))
                .andExpect(jsonPath("$.collectNickname").value(true));
        String nicknameBody = """
                {"username":"%s","password":"password123","passwordCheck":"password123","isPrivacyConsentAgreed":true,
                 "name":"무시됨"%s}
                """;
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(nicknameBody.formatted("nonick", "")))
                .andExpect(jsonPath("$.code").value("NICKNAME_REQUIRED"));
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(nicknameBody.formatted("nick1", ",\"nickname\":\"별빛\"")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(nicknameBody.formatted("nick2", ",\"nickname\":\"별빛\"")))
                .andExpect(jsonPath("$.code").value("DUPLICATE_NICKNAME"));

        MockHttpSession nickMember = login("nick1", "password123", "MEMBER");
        mockMvc.perform(get("/api/auth/me").session(nickMember)).andExpect(jsonPath("$.name").value("별빛"));
        mockMvc.perform(get("/api/members/me").session(nickMember))
                .andExpect(jsonPath("$.nickname").value("별빛"))
                .andExpect(jsonPath("$.name").doesNotExist());
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
