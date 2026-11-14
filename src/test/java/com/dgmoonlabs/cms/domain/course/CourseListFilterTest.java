package com.dgmoonlabs.cms.domain.course;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.JsonNode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseListFilterTest extends ApiTestSupport {
    @Test
    void adminCanFilterByIsCourse() throws Exception {
        MockHttpSession admin = admin();
        long fileId = upload(admin);
        long streamingId = createdId(postJson("/api/admin/streaming", admin, """
                {"title":"1강 영상","fileId":%d,"durationSeconds":100,"completionRate":90}
                """.formatted(fileId)));

        long seriesId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"일반 시리즈","isUsing":true,"isCourse":false,"lessons":[{"title":"1강","streamingId":%d}]}
                """.formatted(streamingId)).andExpect(status().isCreated()));
        long courseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"정식 교육과정","isUsing":true,"isCourse":true,"enrollmentMode":"FREE","lessons":[{"title":"1강","streamingId":%d}]}
                """.formatted(streamingId)).andExpect(status().isCreated()));

        JsonNode seriesOnly = body(getJson("/api/admin/courses?isCourse=false", admin));
        assertContainsOnly(seriesOnly, seriesId, courseId);

        JsonNode coursesOnly = body(getJson("/api/admin/courses?isCourse=true", admin));
        assertContainsOnly(coursesOnly, courseId, seriesId);

        JsonNode all = body(getJson("/api/admin/courses", admin));
        boolean hasSeries = false;
        boolean hasCourse = false;
        for (JsonNode row : all.get("content")) {
            hasSeries |= row.get("id").asLong() == seriesId;
            hasCourse |= row.get("id").asLong() == courseId;
        }
        org.junit.jupiter.api.Assertions.assertTrue(hasSeries && hasCourse);
    }

    private void assertContainsOnly(final JsonNode page, final long expectedId, final long excludedId) {
        boolean hasExpected = false;
        for (JsonNode row : page.get("content")) {
            org.junit.jupiter.api.Assertions.assertNotEquals(excludedId, row.get("id").asLong());
            hasExpected |= row.get("id").asLong() == expectedId;
        }
        org.junit.jupiter.api.Assertions.assertTrue(hasExpected);
    }

    private long upload(final MockHttpSession session) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "lecture.mp4", "application/octet-stream", "0123456789".getBytes());
        return body(mockMvc.perform(multipart("/api/files").file(file).session(session).header("X-CMS-Zone", "admin").with(csrf()))
                .andExpect(status().isCreated())).get("id").asLong();
    }
}
