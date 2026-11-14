package com.dgmoonlabs.cms.domain.course;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QuizStatisticsFlowTest extends ApiTestSupport {
    @Test
    void computesAverageScorePassRateAndPerQuestionWrongRateAcrossAttempts() throws Exception {
        MockHttpSession admin = admin();
        long quizId = createdId(postJson("/api/admin/quizzes", admin, """
                {"title":"분석용 퀴즈","questions":[
                    {"question":"1번 문제","options":["오답","정답"],"answerIndex":1},
                    {"question":"2번 문제","options":["오답","정답"],"answerIndex":1}
                ],"passScore":50}
                """).andExpect(status().isCreated()));
        long courseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"퀴즈분석 과정","isUsing":true,"isCourse":true,"enrollmentMode":"FREE","lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(quizId)).andExpect(status().isCreated()));
        JsonNode course = body(getJson("/api/courses/" + courseId, null));
        long lessonId = course.get("lessons").get(0).get("id").asLong();

        submitAnswers(courseId, lessonId, "quizstata", "[1,1]", 100, true);
        submitAnswers(courseId, lessonId, "quizstatb", "[1,0]", 50, true);
        submitAnswers(courseId, lessonId, "quizstatc", "[0,0]", 0, false);

        JsonNode stats = body(getJson("/api/admin/quizzes/" + quizId + "/statistics", admin));
        org.junit.jupiter.api.Assertions.assertEquals(3, stats.get("totalAttempts").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(3, stats.get("uniqueTestTakers").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(50.0, stats.get("averageScore").asDouble(), 0.01);
        org.junit.jupiter.api.Assertions.assertEquals(66.67, stats.get("passRate").asDouble(), 0.5);

        JsonNode q1 = stats.get("questions").get(0);
        org.junit.jupiter.api.Assertions.assertEquals(3, q1.get("attemptCount").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(1, q1.get("wrongCount").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(33.33, q1.get("wrongRate").asDouble(), 0.5);

        JsonNode q2 = stats.get("questions").get(1);
        org.junit.jupiter.api.Assertions.assertEquals(3, q2.get("attemptCount").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(2, q2.get("wrongCount").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(66.67, q2.get("wrongRate").asDouble(), 0.5);
    }

    private void submitAnswers(final long courseId, final long lessonId, final String username, final String answersJson,
                                final int expectedScore, final boolean expectedPassed) throws Exception {
        MockHttpSession learner = member(username);
        postJson("/api/courses/%d/enroll".formatted(courseId), learner, "");
        postJson("/api/courses/%d/lessons/%d/quiz/start".formatted(courseId, lessonId), learner, "");
        postJson("/api/courses/%d/lessons/%d/quiz".formatted(courseId, lessonId), learner, "{\"answers\":" + answersJson + "}")
                .andExpect(jsonPath("$.score").value(expectedScore))
                .andExpect(jsonPath("$.passed").value(expectedPassed));
    }
}
