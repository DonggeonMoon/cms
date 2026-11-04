package com.dgmoonlabs.cms.domain.content;

import com.dgmoonlabs.cms.domain.content.service.ContentService;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContentWorkflowApiTest extends ApiTestSupport {
    private static final String PASSWORD = "adminpass1!";

    @Autowired
    private ContentService contentService;
    @Autowired
    private EntityManager entityManager;

    private MockHttpSession contentAdmin(final MockHttpSession root, final long gradeId, final String username) throws Exception {
        postJson("/api/admin/admins", root, """
                {"username":"%s","password":"%s","name":"%s","gradeId":%d}
                """.formatted(username, PASSWORD, username, gradeId)).andExpect(status().isCreated());
        return login(username, PASSWORD, "ADMIN");
    }

    @Test
    void contentIsReviewedByAnotherAdminAndPublishedOnSchedule() throws Exception {
        MockHttpSession root = admin();
        postJson("/api/admin/reauthenticate", root, "{\"password\":\"adminpass1\"}").andExpect(status().isNoContent());
        long grade = createdId(postJson("/api/admin/admin-grades", root, "{\"name\":\"콘텐츠\",\"areas\":[\"CONTENT\"]}"));
        MockHttpSession writer = contentAdmin(root, grade, "writer01");
        MockHttpSession reviewer = contentAdmin(root, grade, "review01");

        postJson("/api/admin/contents", writer, "{\"title\":\"공지\",\"contentHtml\":\"<p>새 소식</p>\",\"status\":\"PUBLISHED\"}")
                .andExpect(jsonPath("$.code").value("PUBLISH_REVIEW_REQUIRED"));
        long id = createdId(postJson("/api/admin/contents", writer, "{\"title\":\"공지\",\"contentHtml\":\"<p>새 소식</p>\",\"status\":\"DRAFT\"}"));
        String base = "/api/admin/contents/" + id;

        postJson(base + "/review-request", writer, "{\"publishAt\":\"2000-01-01T00:00:00\"}")
                .andExpect(jsonPath("$.code").value("INVALID_PUBLISH_AT"));
        postJson(base + "/review-request", writer, "{}").andExpect(jsonPath("$.status").value("IN_REVIEW"));
        postJson(base + "/approve", writer, "").andExpect(jsonPath("$.code").value("SELF_REVIEW_NOT_ALLOWED"));
        getJson("/api/admin/contents/reviews", reviewer).andExpect(jsonPath("$.content[0].reviewRequestedBy").value("writer01"));
        postJson(base + "/reject", reviewer, "{\"comment\":\"오타를 고쳐 주세요\"}")
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.reviewComment").value("오타를 고쳐 주세요"));

        String later = LocalDateTime.now().plusDays(1).withNano(0).toString();
        postJson(base + "/review-request", writer, "{\"publishAt\":\"%s\"}".formatted(later)).andExpect(status().isOk());
        putJson(base, writer, "{\"title\":\"공지\",\"contentHtml\":\"<p>바꿈</p>\",\"status\":\"DRAFT\"}").andExpect(status().isNoContent());
        getJson(base, writer).andExpect(jsonPath("$.status").value("DRAFT"));
        postJson(base + "/review-request", writer, "{\"publishAt\":\"%s\"}".formatted(later)).andExpect(status().isOk());
        postJson(base + "/approve", reviewer, "")
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.reviewedBy").value("review01"));
        getJson("/api/contents/" + id, null).andExpect(status().isNotFound());

        contentService.publishScheduled();
        getJson("/api/contents/" + id, null).andExpect(status().isNotFound());
        entityManager.flush();
        entityManager.createQuery("update Content c set c.publishAt = :past where c.id = :id")
                .setParameter("past", LocalDateTime.now().minusMinutes(1)).setParameter("id", id).executeUpdate();
        entityManager.clear();
        contentService.publishScheduled();
        getJson("/api/contents/" + id, null).andExpect(jsonPath("$.contentHtml").value("<p>바꿈</p>"));
        getJson(base, writer).andExpect(jsonPath("$.publishedAt").exists());

        postJson(base + "/unpublish", writer, "").andExpect(jsonPath("$.status").value("DRAFT"));
        getJson("/api/contents/" + id, null).andExpect(status().isNotFound());
        putJson(base, root, "{\"title\":\"공지\",\"contentHtml\":\"<p>바꿈</p>\",\"status\":\"PUBLISHED\"}").andExpect(status().isNoContent());
        getJson("/api/contents/" + id, null).andExpect(status().isOk());
    }
}
