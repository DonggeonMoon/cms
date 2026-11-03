package com.dgmoonlabs.cms.domain.common.user;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.autoconfigure.exclude=",
        "spring.datasource.url=jdbc:h2:mem:cms-session;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class JdbcSessionTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void loginIsKeptInTheDatabaseAndSurvivesOnlyUntilLogout() throws Exception {
        Cookie xsrf = mockMvc.perform(get("/api/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        mockMvc.perform(post("/api/members").cookie(xsrf).header("X-XSRF-TOKEN", xsrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"sessionuser","password":"password123","passwordCheck":"password123",
                                 "isPrivacyConsentAgreed":true,"name":"세션"}
                                """))
                .andExpect(status().isCreated());

        Cookie session = mockMvc.perform(post("/api/auth/login").cookie(xsrf).header("X-XSRF-TOKEN", xsrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"sessionuser\",\"password\":\"password123\",\"userType\":\"MEMBER\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("SESSION");
        assertThat(session).isNotNull();
        assertThat(jdbcTemplate.queryForObject("select count(*) from spring_session", Integer.class)).isPositive();

        mockMvc.perform(get("/api/auth/me").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("sessionuser"));

        Cookie freshXsrf = mockMvc.perform(get("/api/auth/csrf").cookie(session)).andReturn().getResponse().getCookie("XSRF-TOKEN");
        Cookie token = freshXsrf != null ? freshXsrf : xsrf;
        mockMvc.perform(post("/api/auth/logout").cookie(session, token).header("X-XSRF-TOKEN", token.getValue()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/auth/me").cookie(session)).andExpect(status().isUnauthorized());
    }
}
