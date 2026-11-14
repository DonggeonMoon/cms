package com.dgmoonlabs.cms.domain.course;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseNotificationFlowTest extends ApiTestSupport {
    @Test
    void notifiesOnApplicationApprovalAndCompletion() throws Exception {
        MockHttpSession admin = admin();
        long quizId = createdId(postJson("/api/admin/quizzes", admin, """
                {"title":"퀴즈","questions":[{"question":"1+1은?","options":["1","2"],"answerIndex":1}],"passScore":1}
                """).andExpect(status().isCreated()));

        long approvalCourseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"승인제 과정","isUsing":true,"isCourse":true,"enrollmentMode":"APPROVAL","lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(quizId)).andExpect(status().isCreated()));

        MockHttpSession learner = member("notifylearner");
        postJson("/api/courses/%d/enroll".formatted(approvalCourseId), learner, "").andExpect(status().isOk());
        assertHasNotification(learner, "수강 신청이 접수되었습니다");

        JsonNode applications = body(getJson("/api/admin/courses/%d/applications".formatted(approvalCourseId), admin));
        long applicationId = applications.get("content").get(0).get("id").asLong();
        postJson("/api/admin/courses/%d/applications/%d/approve".formatted(approvalCourseId, applicationId), admin, "")
                .andExpect(status().isNoContent());
        assertHasNotification(learner, "수강 신청이 승인되었습니다");

        long freeCourseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"무료 과정","isUsing":true,"isCourse":true,"enrollmentMode":"FREE","lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(quizId)).andExpect(status().isCreated()));
        postJson("/api/courses/%d/enroll".formatted(freeCourseId), learner, "").andExpect(status().isOk());
        JsonNode freeCourse = body(getJson("/api/courses/" + freeCourseId, learner));
        long lessonId = freeCourse.get("lessons").get(0).get("id").asLong();
        postJson("/api/courses/%d/lessons/%d/quiz/start".formatted(freeCourseId, lessonId), learner, "");
        postJson("/api/courses/%d/lessons/%d/quiz".formatted(freeCourseId, lessonId), learner, "{\"answers\":[1]}")
                .andExpect(status().isOk());
        assertHasNotification(learner, "교육과정을 수료했습니다");
    }

    private void assertHasNotification(final MockHttpSession session, final String title) throws Exception {
        JsonNode notifications = body(getJson("/api/members/me/notifications", session));
        boolean found = false;
        for (JsonNode notification : notifications.get("content")) {
            if (notification.get("title").asText().equals(title)) {
                found = true;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(found, "expected notification with title: " + title);
    }
}
