package com.dgmoonlabs.cms.support;

import com.dgmoonlabs.cms.domain.common.user.entity.admin.entity.Admin;
import com.dgmoonlabs.cms.domain.common.user.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class ApiTestSupport {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    protected MockHttpSession admin() throws Exception {
        if (adminRepository.findByUsernameAndDeletedAtIsNull("root").isEmpty()) {
            adminRepository.save(Admin.builder().username("root").password(passwordEncoder.encode("adminpass1")).name("관리자").build());
        }
        return login("root", "adminpass1", "ADMIN");
    }

    protected MockHttpSession member(final String username) throws Exception {
        mockMvc.perform(post("/api/members").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"password123","passwordCheck":"password123","isPrivacyConsentAgreed":true,"name":"%s님"}
                                """.formatted(username, username)))
                .andExpect(status().isCreated());
        return login(username, "password123", "MEMBER");
    }

    protected MockHttpSession login(final String username, final String password, final String userType) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\",\"userType\":\"%s\"}".formatted(username, password, userType)))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }

    protected ResultActions postJson(final String url, final MockHttpSession session, final String body) throws Exception {
        return mockMvc.perform(withSession(post(url), session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    protected ResultActions putJson(final String url, final MockHttpSession session, final String body) throws Exception {
        return mockMvc.perform(withSession(put(url), session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    protected ResultActions deleteJson(final String url, final MockHttpSession session, final String body) throws Exception {
        var request = withSession(delete(url), session).with(csrf());
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(request);
    }

    protected ResultActions getJson(final String url, final MockHttpSession session) throws Exception {
        return mockMvc.perform(withSession(get(url), session));
    }

    protected long createdId(final ResultActions result) {
        String location = result.andReturn().getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }

    protected JsonNode body(final ResultActions result) throws Exception {
        return jsonMapper.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder withSession(
            final org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder, final MockHttpSession session) {
        if (session == null) {
            return builder;
        }
        builder.session(session);
        if (session.getAttribute("CMS_SECURITY_CONTEXT_ADMIN") != null) {
            builder.header("X-CMS-Zone", "admin");
        }
        return builder;
    }
}
