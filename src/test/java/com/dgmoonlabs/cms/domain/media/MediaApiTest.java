package com.dgmoonlabs.cms.domain.media;

import com.dgmoonlabs.cms.domain.media.entity.WatchedRanges;
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

class MediaApiTest extends ApiTestSupport {
    @Test
    void progressCountsOnlyPlayedSecondsAndCompletesAtTheThreshold() throws Exception {
        MockHttpSession admin = admin();
        long fileId = upload(admin, "spring.mp3");
        long mediaId = createdId(postJson("/api/admin/media", admin, """
                {"title":"봄 노래","fileId":%d,"durationSeconds":100,"completionRate":80}
                """.formatted(fileId)).andExpect(status().isCreated()));
        postJson("/api/admin/media", admin, """
                {"title":"현장 영상","fileId":%d,"durationSeconds":60}
                """.formatted(upload(admin, "field.mp4"))).andExpect(status().isCreated());

        getJson("/api/media?mediaType=AUDIO", null)
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("봄 노래"));
        getJson("/api/media", null).andExpect(jsonPath("$.content.length()").value(2));

        getJson("/api/media/" + mediaId, null)
                .andExpect(jsonPath("$.mediaType").value("AUDIO"))
                .andExpect(jsonPath("$.myProgress").doesNotExist());
        postJson("/api/media/" + mediaId + "/start", null, "{}").andExpect(status().isUnauthorized());

        MockHttpSession member = member("learner1");
        postJson("/api/media/" + mediaId + "/start", member, "{}")
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.progressRate").value(0));

        postJson("/api/media/" + mediaId + "/progress", member, """
                {"ranges":[{"start":0,"end":40},{"start":30,"end":50}],"position":50}
                """).andExpect(jsonPath("$.progressRate").value(50))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        postJson("/api/media/" + mediaId + "/progress", member, """
                {"ranges":[{"start":0,"end":50}],"position":95}
                """).andExpect(jsonPath("$.progressRate").value(50));
        postJson("/api/media/" + mediaId + "/progress", member, """
                {"ranges":[{"start":50,"end":80.4}],"position":80}
                """).andExpect(jsonPath("$.progressRate").value(80))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());

        getJson("/api/media/me", member)
                .andExpect(jsonPath("$.content[0].title").value("봄 노래"))
                .andExpect(jsonPath("$.content[0].progress.progressRate").value(80))
                .andExpect(jsonPath("$.content[0].progress.lastPositionSeconds").value(80));
        getJson("/api/admin/media/" + mediaId + "/learners", admin)
                .andExpect(jsonPath("$.content[0].username").value("learner1"))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));
    }

    @Test
    void mediaNeedsAPlayableFileAndServesItInRanges() throws Exception {
        MockHttpSession admin = admin();
        long documentId = upload(admin, "notes.txt");
        postJson("/api/admin/media", admin, """
                {"title":"자료","fileId":%d,"durationSeconds":10}
                """.formatted(documentId)).andExpect(jsonPath("$.code").value("MEDIA_FILE_REQUIRED"));

        long videoId = upload(admin, "clip.mp4");
        postJson("/api/admin/media", admin, """
                {"title":"영상","fileId":%d,"thumbnailFileId":%d,"durationSeconds":10}
                """.formatted(videoId, documentId)).andExpect(jsonPath("$.code").value("MEDIA_THUMBNAIL_IMAGE_REQUIRED"));
        long imageId = upload(admin, "cover.png");
        long mediaId = createdId(postJson("/api/admin/media", admin, """
                {"title":"영상","fileId":%d,"thumbnailFileId":%d,"durationSeconds":10}
                """.formatted(videoId, imageId)).andExpect(status().isCreated()));
        getJson("/api/media/" + mediaId, null)
                .andExpect(jsonPath("$.thumbnailUrl").value("/api/files/" + imageId + "/thumbnail"))
                .andExpect(jsonPath("$.posterUrl").value("/api/files/" + imageId + "/view"));

        getJson("/api/media/" + mediaId, null).andExpect(jsonPath("$.mediaUrl").value("/api/media/" + mediaId + "/stream"));
        mockMvc.perform(get("/api/media/" + mediaId + "/stream").header(HttpHeaders.RANGE, "bytes=0-3"))
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
