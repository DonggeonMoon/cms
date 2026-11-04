package com.dgmoonlabs.cms.domain.course;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.JsonNode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseApiTest extends ApiTestSupport {
    private static final String QUIZ = """
            [{"question":"1+1은?","options":["1","2","3"],"answerIndex":1},
             {"question":"하늘 색은?","options":["파랑","빨강"],"answerIndex":0}]""";

    @Test
    void lessonsCompleteByWatchingAndPassingQuizzesInOrder() throws Exception {
        MockHttpSession admin = admin();
        long mediaId = createdId(postJson("/api/admin/media", admin, """
                {"title":"1강 영상","fileId":%d,"durationSeconds":100,"completionRate":90}
                """.formatted(upload(admin))));

        postJson("/api/admin/courses", admin, """
                {"title":"빈 차시","isUsing":true,"lessons":[{"title":"아무것도 없음"}]}
                """).andExpect(jsonPath("$.code").value("INVALID_LESSON"));
        postJson("/api/admin/courses", admin, """
                {"title":"정답 오류","isUsing":true,"lessons":[{"title":"퀴즈","quiz":[{"question":"?","options":["a","b"],"answerIndex":5}]}]}
                """).andExpect(jsonPath("$.code").value("INVALID_LESSON"));
        long courseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"기초 과정","sequential":true,"isUsing":true,"lessons":[
                  {"title":"1차시 강의","mediaId":%d},
                  {"title":"2차시 확인 문제","quiz":%s,"passScore":100}]}
                """.formatted(mediaId, QUIZ)).andExpect(status().isCreated()));

        getJson("/api/admin/courses/" + courseId, admin).andExpect(jsonPath("$.lessons[1].quiz[0].answerIndex").value(1));
        JsonNode course = body(getJson("/api/courses/" + courseId, null)
                .andExpect(jsonPath("$.lessonCount").value(2))
                .andExpect(jsonPath("$.lessons[0].mediaTitle").value("1강 영상"))
                .andExpect(jsonPath("$.lessons[1].quiz[0].answerIndex").doesNotExist())
                .andExpect(jsonPath("$.lessons[1].passScore").value(100)));
        long quizLesson = course.get("lessons").get(1).get("id").asLong();
        String quizUrl = "/api/courses/%d/lessons/%d/quiz".formatted(courseId, quizLesson);

        MockHttpSession learner = member("student1");
        postJson(quizUrl, learner, "{\"answers\":[1,0]}").andExpect(jsonPath("$.code").value("COURSE_NOT_ENROLLED"));
        postJson("/api/courses/%d/enroll".formatted(courseId), learner, "")
                .andExpect(jsonPath("$.myProgress.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.lessons[1].locked").value(true));
        postJson(quizUrl, learner, "{\"answers\":[1,0]}").andExpect(jsonPath("$.code").value("LESSON_LOCKED"));

        postJson("/api/media/" + mediaId + "/start", learner, "{}").andExpect(status().isOk());
        postJson("/api/media/" + mediaId + "/progress", learner, """
                {"ranges":[{"start":0,"end":95}],"position":95}
                """).andExpect(jsonPath("$.status").value("COMPLETED"));
        getJson("/api/courses/" + courseId, learner)
                .andExpect(jsonPath("$.lessons[0].completed").value(true))
                .andExpect(jsonPath("$.lessons[1].locked").value(false))
                .andExpect(jsonPath("$.myProgress.completedLessons").value(1));

        postJson(quizUrl, learner, "{\"answers\":[1]}").andExpect(status().isBadRequest());
        postJson(quizUrl, learner, "{\"answers\":[1,1]}")
                .andExpect(jsonPath("$.score").value(50))
                .andExpect(jsonPath("$.passed").value(false))
                .andExpect(jsonPath("$.correct[1]").value(false))
                .andExpect(jsonPath("$.myProgress.status").value("IN_PROGRESS"));
        postJson(quizUrl, learner, "{\"answers\":[1,0]}")
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.attempts").value(2))
                .andExpect(jsonPath("$.myProgress.status").value("COMPLETED"))
                .andExpect(jsonPath("$.myProgress.completedAt").exists());

        getJson("/api/courses/me", learner)
                .andExpect(jsonPath("$.content[0].title").value("기초 과정"))
                .andExpect(jsonPath("$.content[0].myProgress.completedLessons").value(2));
        getJson("/api/admin/courses/%d/learners".formatted(courseId), admin)
                .andExpect(jsonPath("$.content[0].username").value("student1"))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.content[0].totalLessons").value(2));

        putJson("/api/admin/courses/" + courseId, admin, """
                {"title":"기초 과정","sequential":true,"isUsing":true,"lessons":[
                  {"id":%d,"title":"확인 문제를 앞으로","quiz":%s,"passScore":100},
                  {"title":"1차시 강의","mediaId":%d}]}
                """.formatted(quizLesson, QUIZ, mediaId)).andExpect(status().isNoContent());
        getJson("/api/courses/" + courseId, learner)
                .andExpect(jsonPath("$.lessons[0].title").value("확인 문제를 앞으로"))
                .andExpect(jsonPath("$.lessons[0].quizPassed").value(true))
                .andExpect(jsonPath("$.lessons[1].completed").value(true));
    }

    private long upload(final MockHttpSession session) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "lecture.mp4", "application/octet-stream", "0123456789".getBytes());
        return body(mockMvc.perform(multipart("/api/files").file(file).session(session).header("X-CMS-Zone", "admin").with(csrf()))
                .andExpect(status().isCreated())).get("id").asLong();
    }
}
