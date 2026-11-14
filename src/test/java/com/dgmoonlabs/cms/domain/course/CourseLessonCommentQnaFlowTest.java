package com.dgmoonlabs.cms.domain.course;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseLessonCommentQnaFlowTest extends ApiTestSupport {
    @Test
    void listsUnansweredQuestionsAndNotifiesTheAskerOnceAnswered() throws Exception {
        MockHttpSession admin = admin();
        long quizId = createdId(postJson("/api/admin/quizzes", admin, """
                {"title":"퀴즈","questions":[{"question":"1+1은?","options":["1","2"],"answerIndex":1}],"passScore":1}
                """).andExpect(status().isCreated()));
        long courseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"Q&A 과정","isUsing":true,"isCourse":true,"enrollmentMode":"FREE","lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(quizId)).andExpect(status().isCreated()));
        JsonNode course = body(getJson("/api/courses/" + courseId, null));
        long lessonId = course.get("lessons").get(0).get("id").asLong();

        MockHttpSession asker = member("qnaasker");
        postJson("/api/courses/%d/enroll".formatted(courseId), asker, "");
        long questionId = createdId(postJson("/api/courses/%d/lessons/%d/comments".formatted(courseId, lessonId), asker,
                "{\"content\":\"이 부분 질문있어요\"}").andExpect(status().isCreated()));

        JsonNode unanswered = body(getJson("/api/admin/courses/qna/unanswered", admin));
        boolean found = false;
        for (JsonNode row : unanswered.get("content")) {
            if (row.get("commentId").asLong() == questionId) {
                found = true;
                org.junit.jupiter.api.Assertions.assertEquals(courseId, row.get("courseId").asLong());
                org.junit.jupiter.api.Assertions.assertEquals(lessonId, row.get("lessonId").asLong());
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(found, "question should be listed as unanswered");

        postJson("/api/courses/%d/lessons/%d/comments".formatted(courseId, lessonId), admin,
                "{\"parentId\":%d,\"content\":\"이렇게 해보세요\"}".formatted(questionId)).andExpect(status().isCreated());

        JsonNode afterAnswer = body(getJson("/api/admin/courses/qna/unanswered", admin));
        for (JsonNode row : afterAnswer.get("content")) {
            org.junit.jupiter.api.Assertions.assertNotEquals(questionId, row.get("commentId").asLong());
        }

        JsonNode notifications = body(getJson("/api/members/me/notifications", asker));
        boolean notified = false;
        for (JsonNode notification : notifications.get("content")) {
            if (notification.get("title").asText().equals("질문에 답변이 등록되었습니다")) {
                notified = true;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(notified, "asker should be notified of the answer");
    }
}
