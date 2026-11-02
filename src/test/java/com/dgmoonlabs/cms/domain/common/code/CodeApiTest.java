package com.dgmoonlabs.cms.domain.common.code;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class CodeApiTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void manageCodeTree() throws Exception {
        String rootLocation = create("""
                {"name":"게시판 분류","englishName":"BOARD_CATEGORY","order":1,"isUsing":true}
                """);
        long rootId = idOf(rootLocation);

        create("""
                {"parentId":%d,"name":"공지","order":2,"isUsing":true}
                """.formatted(rootId));
        create("""
                {"parentId":%d,"name":"자유","order":1,"isUsing":true}
                """.formatted(rootId));
        create("""
                {"parentId":%d,"name":"미사용","order":3,"isUsing":false}
                """.formatted(rootId));

        mockMvc.perform(get("/api/codes").param("parentId", String.valueOf(rootId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("자유"))
                .andExpect(jsonPath("$[0].depth").value(1));

        mockMvc.perform(get("/api/admin/codes").param("parentId", String.valueOf(rootId)))
                .andExpect(jsonPath("$.page.totalElements").value(3));
        mockMvc.perform(get("/api/admin/codes").param("keyword", "board_cat"))
                .andExpect(jsonPath("$.content[0].id").value(rootId));

        mockMvc.perform(delete(rootLocation).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CODE_HAS_CHILDREN"));

        mockMvc.perform(put(rootLocation).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"분류","order":5,"isUsing":false}
                                """))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(rootLocation))
                .andExpect(jsonPath("$.name").value("분류"))
                .andExpect(jsonPath("$.isUsing").value(false));

        mockMvc.perform(get("/api/admin/codes/999999")).andExpect(status().isNotFound());
    }

    private String create(final String body) throws Exception {
        return mockMvc.perform(post("/api/admin/codes").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
    }

    private long idOf(final String location) {
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }
}
