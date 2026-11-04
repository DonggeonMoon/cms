package com.dgmoonlabs.cms.domain.common.code;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CodeApiTest extends ApiTestSupport {
    private MockHttpSession admin;

    @BeforeEach
    void loginAsAdmin() throws Exception {
        admin = admin();
    }

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

        getJson("/api/codes?parentId=" + rootId, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("자유"))
                .andExpect(jsonPath("$[0].depth").value(1));

        getJson("/api/admin/codes?parentId=" + rootId, admin)
                .andExpect(jsonPath("$.page.totalElements").value(3));
        getJson("/api/admin/codes?keyword=board_cat", admin)
                .andExpect(jsonPath("$.content[0].id").value(rootId));

        deleteJson(rootLocation, admin, null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CODE_HAS_CHILDREN"));

        putJson(rootLocation, admin, """
                {"name":"분류","order":5,"isUsing":false}
                """)
                .andExpect(status().isNoContent());
        getJson(rootLocation, admin)
                .andExpect(jsonPath("$.name").value("분류"))
                .andExpect(jsonPath("$.isUsing").value(false));

        getJson("/api/admin/codes/999999", admin).andExpect(status().isNotFound());
    }

    private String create(final String body) throws Exception {
        return postJson("/api/admin/codes", admin, body)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
    }

    private long idOf(final String location) {
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }
}
