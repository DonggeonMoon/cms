package com.dgmoonlabs.cms.domain.admin.account;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminAccountApiTest extends ApiTestSupport {
    private static final String PASSWORD = "adminpass1!";

    private void reauthenticate(final MockHttpSession admin) throws Exception {
        postJson("/api/admin/reauthenticate", admin, "{\"password\":\"adminpass1\"}").andExpect(status().isNoContent());
    }

    @Test
    void onlySuperAdminsManageAdminsAfterReauthenticating() throws Exception {
        MockHttpSession root = admin();
        getJson("/api/admin/me/permissions", root).andExpect(jsonPath("$.superAdmin").value(true));
        getJson("/api/admin/admins", root).andExpect(jsonPath("$.code").value("REAUTHENTICATION_REQUIRED"));
        reauthenticate(root);

        long contentGrade = createdId(postJson("/api/admin/admin-grades", root, """
                {"name":"콘텐츠 담당","areas":["CONTENT","STATUS"]}
                """).andExpect(status().isCreated()));
        long editorId = createdId(postJson("/api/admin/admins", root, """
                {"username":"editor01","password":"%s","name":"편집자","gradeId":%d}
                """.formatted(PASSWORD, contentGrade)).andExpect(status().isCreated()));
        postJson("/api/admin/admins", root, """
                {"username":"editor01","password":"%s","name":"중복"}
                """.formatted(PASSWORD)).andExpect(jsonPath("$.code").value("DUPLICATE_USERNAME"));
        createdId(postJson("/api/admin/admins", root, """
                {"username":"legacy01","password":"%s","name":"등급 없음"}
                """.formatted(PASSWORD)));

        JsonNode admins = body(getJson("/api/admin/admins", root));
        long rootId = admins.get(0).get("id").asLong();
        getJson("/api/admin/admins", root)
                .andExpect(jsonPath("$[0].superAdmin").value(true))
                .andExpect(jsonPath("$[1].gradeName").value("콘텐츠 담당"));

        MockHttpSession editor = login("editor01", PASSWORD, "ADMIN");
        getJson("/api/admin/me/permissions", editor)
                .andExpect(jsonPath("$.superAdmin").value(false))
                .andExpect(jsonPath("$.areas.length()").value(2));
        getJson("/api/admin/contents", editor).andExpect(status().isOk());
        getJson("/api/admin/banners", editor).andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        postJson("/api/admin/reauthenticate", editor, "{\"password\":\"%s\"}".formatted(PASSWORD)).andExpect(status().isNoContent());
        getJson("/api/admin/admins", editor).andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        MockHttpSession legacy = login("legacy01", PASSWORD, "ADMIN");
        getJson("/api/admin/banners", legacy).andExpect(status().isOk());
        getJson("/api/admin/api-keys/options", legacy).andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        putJson("/api/admin/admins/%d/grade".formatted(rootId), root, "{\"gradeId\":null}")
                .andExpect(jsonPath("$.code").value("LAST_SUPER_ADMIN"));
        deleteJson("/api/admin/admins/" + rootId, root, null).andExpect(jsonPath("$.code").value("CANNOT_DELETE_SELF"));
        deleteJson("/api/admin/admin-grades/" + contentGrade, root, null).andExpect(jsonPath("$.code").value("ADMIN_GRADE_IN_USE"));
        long superGrade = admins.get(0).get("gradeId").asLong();
        deleteJson("/api/admin/admin-grades/" + superGrade, root, null).andExpect(jsonPath("$.code").value("ADMIN_GRADE_IN_USE"));

        putJson("/api/admin/admins/%d/grade".formatted(editorId), root, "{\"gradeId\":%d}".formatted(superGrade))
                .andExpect(status().isNoContent());
        getJson("/api/admin/banners", editor).andExpect(status().isOk());
        putJson("/api/admin/admins/%d/grade".formatted(rootId), root, "{\"gradeId\":%d}".formatted(contentGrade))
                .andExpect(status().isNoContent());
        getJson("/api/admin/admin-grades", root).andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }
}
