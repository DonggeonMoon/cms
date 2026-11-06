package com.dgmoonlabs.cms.domain.streaming;

import com.dgmoonlabs.cms.domain.streaming.entity.WatchedRanges;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StreamingApiTest extends ApiTestSupport {
    @Test
    void progressCountsOnlyPlayedSecondsAndCompletesAtTheThreshold() throws Exception {
        MockHttpSession admin = admin();
        long fileId = upload(admin, "spring.mp3");
        long streamingId = createdId(postJson("/api/admin/streaming", admin, """
                {"title":"봄 노래","fileId":%d,"durationSeconds":100,"completionRate":80}
                """.formatted(fileId)).andExpect(status().isCreated()));
        postJson("/api/admin/streaming", admin, """
                {"title":"현장 영상","fileId":%d,"durationSeconds":60}
                """.formatted(upload(admin, "field.mp4"))).andExpect(status().isCreated());

        getJson("/api/streaming?mediaType=AUDIO", null)
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("봄 노래"));
        getJson("/api/streaming", null).andExpect(jsonPath("$.content.length()").value(2));

        getJson("/api/streaming/" + streamingId, null)
                .andExpect(jsonPath("$.mediaType").value("AUDIO"))
                .andExpect(jsonPath("$.myProgress").doesNotExist());
        postJson("/api/streaming/" + streamingId + "/start", null, "{}").andExpect(status().isUnauthorized());

        MockHttpSession member = member("learner1");
        postJson("/api/streaming/" + streamingId + "/start", member, "{}")
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.progressRate").value(0));

        postJson("/api/streaming/" + streamingId + "/progress", member, """
                {"ranges":[{"start":0,"end":40},{"start":30,"end":50}],"position":50}
                """).andExpect(jsonPath("$.progressRate").value(50))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        postJson("/api/streaming/" + streamingId + "/progress", member, """
                {"ranges":[{"start":0,"end":50}],"position":95}
                """).andExpect(jsonPath("$.progressRate").value(50));
        postJson("/api/streaming/" + streamingId + "/progress", member, """
                {"ranges":[{"start":50,"end":80.4}],"position":80}
                """).andExpect(jsonPath("$.progressRate").value(80))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());

        getJson("/api/streaming/me", member)
                .andExpect(jsonPath("$.content[0].title").value("봄 노래"))
                .andExpect(jsonPath("$.content[0].progress.progressRate").value(80))
                .andExpect(jsonPath("$.content[0].progress.lastPositionSeconds").value(80));
        getJson("/api/admin/streaming/" + streamingId + "/learners", admin)
                .andExpect(jsonPath("$.content[0].username").value("learner1"))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));
    }

    @Test
    void streamingNeedsAPlayableFileAndServesItInRanges() throws Exception {
        MockHttpSession admin = admin();
        long documentId = upload(admin, "notes.txt");
        postJson("/api/admin/streaming", admin, """
                {"title":"자료","fileId":%d,"durationSeconds":10}
                """.formatted(documentId)).andExpect(jsonPath("$.code").value("STREAMING_FILE_REQUIRED"));

        long videoId = upload(admin, "clip.mp4");
        postJson("/api/admin/streaming", admin, """
                {"title":"영상","fileId":%d,"thumbnailFileId":%d,"durationSeconds":10}
                """.formatted(videoId, documentId)).andExpect(jsonPath("$.code").value("STREAMING_THUMBNAIL_IMAGE_REQUIRED"));
        long imageId = upload(admin, "cover.png");
        long streamingId = createdId(postJson("/api/admin/streaming", admin, """
                {"title":"영상","fileId":%d,"thumbnailFileId":%d,"durationSeconds":10}
                """.formatted(videoId, imageId)).andExpect(status().isCreated()));
        getJson("/api/streaming/" + streamingId, null)
                .andExpect(jsonPath("$.thumbnailUrl").value("/api/files/" + imageId + "/thumbnail"))
                .andExpect(jsonPath("$.posterUrl").value("/api/files/" + imageId + "/view"));

        getJson("/api/streaming/" + streamingId, null).andExpect(jsonPath("$.streamingUrl").value("/api/streaming/" + streamingId + "/stream"));
        mockMvc.perform(get("/api/streaming/" + streamingId + "/stream").header(HttpHeaders.RANGE, "bytes=0-3"))
                .andExpect(status().isPartialContent());
        mockMvc.perform(get("/api/files/" + videoId + "/view")).andExpect(status().isNotFound());
    }

    @Test
    void watchedRangesMergeOverlaps() {
        WatchedRanges ranges = WatchedRanges.parse("0-10,20-30").add(5, 25, 100).add(90, 150, 100);
        assertThat(ranges.format()).isEqualTo("0-30,90-100");
        assertThat(ranges.totalSeconds()).isEqualTo(40);
    }

    private long upload(final MockHttpSession session, final String name) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", name, "application/octet-stream", "0123456789".getBytes());
        return body(mockMvc.perform(multipart("/api/files").file(file).session(session).header("X-CMS-Zone", "admin").with(csrf()))
                .andExpect(status().isCreated())).get("id").asLong();
    }
}
