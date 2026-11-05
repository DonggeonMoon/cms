package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClassAttendanceFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    @Test
    void adminMarksAttendanceForEnrolledStudentsAndTheStudentSeesHerHistory() throws Exception {
        MockHttpSession admin = admin();
        long classProduct = createdId(postJson("/api/admin/products", admin, """
                {"name":"화요일 영어반 수강료","price":100000,"saleType":"SUBSCRIPTION","billingCycle":"MONTHLY","isEnabled":true}
                """));

        MockHttpSession student = member("student1");
        long orderId = body(postJson("/api/orders", student, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(classProduct, RECEIVER))).get("id").asLong();
        postJson("/api/payments", student, """
                {"targetType":"ORDER","targetId":%d,"amount":100000,"method":"CARD"}
                """.formatted(orderId)).andExpect(status().isOk());
        long subscriptionId = body(getJson("/api/subscriptions/me", student)).get("content").get(0).get("id").asLong();

        LocalDate classDate = LocalDate.now();
        getJson("/api/admin/class-attendance/roster?productId=" + classProduct + "&date=" + classDate, admin)
                .andExpect(jsonPath("$[0].subscriptionId").value(subscriptionId))
                .andExpect(jsonPath("$[0].memberName").exists())
                .andExpect(jsonPath("$[0].present").doesNotExist());

        postJson("/api/admin/class-attendance/mark", admin, """
                {"subscriptionId":%d,"attendedOn":"%s","present":true,"note":"정상 출석"}
                """.formatted(subscriptionId, classDate))
                .andExpect(jsonPath("$.present").value(true));

        getJson("/api/admin/class-attendance/roster?productId=" + classProduct + "&date=" + classDate, admin)
                .andExpect(jsonPath("$[0].present").value(true))
                .andExpect(jsonPath("$[0].note").value("정상 출석"));

        // 같은 날 다시 매기면 덮어쓴다 (결석으로 정정)
        postJson("/api/admin/class-attendance/mark", admin, """
                {"subscriptionId":%d,"attendedOn":"%s","present":false,"note":"지각 결석 처리"}
                """.formatted(subscriptionId, classDate))
                .andExpect(jsonPath("$.present").value(false));

        var mine = body(getJson("/api/class-attendance/me?subscriptionId=" + subscriptionId, student));
        assertThat(mine.get("content").size()).isEqualTo(1);
        assertThat(mine.get("content").get(0).get("attendedOn").asText()).isEqualTo(classDate.toString());
        assertThat(mine.get("content").get(0).get("present").asBoolean()).isFalse();
        assertThat(mine.get("content").get(0).get("note").asText()).isEqualTo("지각 결석 처리");

        // 다른 학생은 이 구독 출결을 볼 수 없다
        MockHttpSession otherStudent = member("student2");
        getJson("/api/class-attendance/me?subscriptionId=" + subscriptionId, otherStudent)
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
