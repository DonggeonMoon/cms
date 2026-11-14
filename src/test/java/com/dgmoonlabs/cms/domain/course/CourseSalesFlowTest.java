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

class CourseSalesFlowTest extends ApiTestSupport {
    private static final String ORDERER = "\"receiverName\":\"학생\",\"phoneNumber\":\"010-1234-5678\"";

    @Test
    void paidCourseIsBoughtThroughAHiddenProductAndAutoApprovesEnrollment() throws Exception {
        MockHttpSession admin = admin();
        long fileId = upload(admin);
        long streamingId = createdId(postJson("/api/admin/streaming", admin, """
                {"title":"1강 영상","fileId":%d,"durationSeconds":100,"completionRate":90}
                """.formatted(fileId)));

        postJson("/api/admin/courses", admin, """
                {"title":"유료 과정","isUsing":true,"isCourse":true,"paid":true,"lessons":[{"title":"1강","streamingId":%d}]}
                """.formatted(streamingId)).andExpect(jsonPath("$.code").value("COURSE_PRICE_REQUIRED"));
        long courseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"유료 과정","isUsing":true,"isCourse":true,"paid":true,"price":20000,
                 "lessons":[{"title":"1강","streamingId":%d}]}
                """.formatted(streamingId)).andExpect(status().isCreated()));

        JsonNode detail = body(getJson("/api/courses/" + courseId, null)
                .andExpect(jsonPath("$.paid").value(true))
                .andExpect(jsonPath("$.price").value(20000))
                .andExpect(jsonPath("$.myProgress").doesNotExist()));
        long productId = detail.get("productId").asLong();
        getJson("/api/products", null).andExpect(jsonPath("$.page.totalElements").value(0));

        MockHttpSession learner = member("buyer1");
        postJson("/api/courses/%d/enroll".formatted(courseId), learner, "")
                .andExpect(jsonPath("$.code").value("COURSE_PURCHASE_REQUIRED"));

        long orderId = body(postJson("/api/orders", learner, "{\"productId\":%d,\"quantity\":1,%s}".formatted(productId, ORDERER))
                .andExpect(jsonPath("$.shippingFee").value(0))).get("id").asLong();
        postJson("/api/payments", learner, """
                {"targetType":"ORDER","targetId":%d,"amount":20000,"method":"CARD"}
                """.formatted(orderId)).andExpect(jsonPath("$.status").value("APPROVED"));

        getJson("/api/courses/" + courseId, learner)
                .andExpect(jsonPath("$.myProgress.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.myApplicationStatus").value("APPROVED"))
                .andExpect(jsonPath("$.myAccessOpen").value(true));

        // 환불 정책(진도율) 제한이 없으므로 관리자가 바로 환불 처리할 수 있고, 환불되면 수강 권한이 사라진다.
        postJson("/api/admin/courses/%d/learners/%d/refund".formatted(courseId, memberIdOf(learner)), admin, "")
                .andExpect(status().isNoContent());
        getJson("/api/courses/" + courseId, learner).andExpect(jsonPath("$.myAccessOpen").value(false));

        // 환불 정책 기준(진도율)을 넘기면 환불이 막힌다.
        long quizId = createdId(postJson("/api/admin/quizzes", admin, """
                {"title":"퀴즈","questions":[{"question":"1+1은?","options":["1","2"],"answerIndex":1}],"passScore":50}
                """).andExpect(status().isCreated()));
        long policyCourseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"정책 과정","isUsing":true,"isCourse":true,"paid":true,"price":10000,"refundableProgressBelowPercent":50,
                 "lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(quizId)).andExpect(status().isCreated()));
        JsonNode policyCourse = body(getJson("/api/courses/" + policyCourseId, null));
        long policyProductId = policyCourse.get("productId").asLong();
        MockHttpSession learner2 = member("buyer2");
        long order2 = body(postJson("/api/orders", learner2, "{\"productId\":%d,\"quantity\":1,%s}".formatted(policyProductId, ORDERER)))
                .get("id").asLong();
        postJson("/api/payments", learner2, """
                {"targetType":"ORDER","targetId":%d,"amount":10000,"method":"CARD"}
                """.formatted(order2)).andExpect(jsonPath("$.status").value("APPROVED"));

        JsonNode afterPurchase = body(getJson("/api/courses/" + policyCourseId, learner2));
        long lessonId = afterPurchase.get("lessons").get(0).get("id").asLong();
        postJson("/api/courses/%d/lessons/%d/quiz/start".formatted(policyCourseId, lessonId), learner2, "");
        postJson("/api/courses/%d/lessons/%d/quiz".formatted(policyCourseId, lessonId), learner2, "{\"answers\":[1]}")
                .andExpect(jsonPath("$.passed").value(true));

        postJson("/api/admin/courses/%d/learners/%d/refund".formatted(policyCourseId, memberIdOf(learner2)), admin, "")
                .andExpect(jsonPath("$.code").value("COURSE_REFUND_NOT_ALLOWED"));
    }

    private long memberIdOf(final MockHttpSession session) throws Exception {
        return body(getJson("/api/members/me", session)).get("id").asLong();
    }

    private long upload(final MockHttpSession session) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "lecture.mp4", "application/octet-stream", "0123456789".getBytes());
        return body(mockMvc.perform(multipart("/api/files").file(file).session(session).header("X-CMS-Zone", "admin").with(csrf()))
                .andExpect(status().isCreated())).get("id").asLong();
    }
}
