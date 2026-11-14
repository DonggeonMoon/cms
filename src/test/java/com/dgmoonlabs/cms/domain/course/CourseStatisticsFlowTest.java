package com.dgmoonlabs.cms.domain.course;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseStatisticsFlowTest extends ApiTestSupport {
    private static final String ORDERER = "\"receiverName\":\"학생\",\"phoneNumber\":\"010-1234-5678\"";

    @Test
    void statisticsAggregateEnrollmentCompletionAndSalesAcrossCourses() throws Exception {
        MockHttpSession admin = admin();
        long quizId = createdId(postJson("/api/admin/quizzes", admin, """
                {"title":"퀴즈","questions":[{"question":"1+1은?","options":["1","2"],"answerIndex":1}],"passScore":1}
                """).andExpect(status().isCreated()));

        long freeCourseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"무료 과정","isUsing":true,"isCourse":true,"enrollmentMode":"FREE","lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(quizId)).andExpect(status().isCreated()));
        MockHttpSession learnerA = member("stata");
        postJson("/api/courses/%d/enroll".formatted(freeCourseId), learnerA, "");
        JsonNode freeCourse = body(getJson("/api/courses/" + freeCourseId, learnerA));
        long lessonId = freeCourse.get("lessons").get(0).get("id").asLong();
        postJson("/api/courses/%d/lessons/%d/quiz/start".formatted(freeCourseId, lessonId), learnerA, "");
        postJson("/api/courses/%d/lessons/%d/quiz".formatted(freeCourseId, lessonId), learnerA, "{\"answers\":[1]}");

        MockHttpSession learnerB = member("statb");
        postJson("/api/courses/%d/enroll".formatted(freeCourseId), learnerB, "");

        long paidQuizId = createdId(postJson("/api/admin/quizzes", admin, """
                {"title":"퀴즈2","questions":[{"question":"1+1은?","options":["1","2"],"answerIndex":1}],"passScore":50}
                """).andExpect(status().isCreated()));
        long paidCourseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"유료 과정","isUsing":true,"isCourse":true,"paid":true,"price":10000,"lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(paidQuizId)).andExpect(status().isCreated()));
        JsonNode paidCourse = body(getJson("/api/courses/" + paidCourseId, null));
        long productId = paidCourse.get("productId").asLong();
        MockHttpSession buyer = member("statbuyer");
        long orderId = body(postJson("/api/orders", buyer, "{\"productId\":%d,\"quantity\":1,%s}".formatted(productId, ORDERER)))
                .get("id").asLong();
        postJson("/api/payments", buyer, """
                {"targetType":"ORDER","targetId":%d,"amount":10000,"method":"CARD"}
                """.formatted(orderId));

        JsonNode statistics = body(getJson("/api/admin/courses/statistics", admin));
        JsonNode freeRow = findRow(statistics, freeCourseId);
        org.junit.jupiter.api.Assertions.assertEquals(2, freeRow.get("totalEnrolled").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(1, freeRow.get("completed").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(50.0, freeRow.get("completionRate").asDouble());

        JsonNode paidRow = findRow(statistics, paidCourseId);
        org.junit.jupiter.api.Assertions.assertEquals(1, paidRow.get("totalEnrolled").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(10000, paidRow.get("revenue").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(1, paidRow.get("paidCount").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(0, paidRow.get("refundedCount").asInt());

        org.junit.jupiter.api.Assertions.assertTrue(statistics.get("totalRevenue").asInt() >= 10000);

        postJson("/api/admin/courses/%d/learners/%d/refund".formatted(paidCourseId, memberIdOf(buyer)), admin, "")
                .andExpect(status().isNoContent());
        JsonNode afterRefund = body(getJson("/api/admin/courses/statistics", admin));
        JsonNode paidRowAfterRefund = findRow(afterRefund, paidCourseId);
        org.junit.jupiter.api.Assertions.assertEquals(0, paidRowAfterRefund.get("revenue").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(1, paidRowAfterRefund.get("refundedCount").asInt());
    }

    private long memberIdOf(final MockHttpSession session) throws Exception {
        return body(getJson("/api/members/me", session)).get("id").asLong();
    }

    private JsonNode findRow(final JsonNode statistics, final long courseId) {
        for (JsonNode row : statistics.get("rows")) {
            if (row.get("courseId").asLong() == courseId) {
                return row;
            }
        }
        throw new AssertionError("No row for course " + courseId);
    }
}
