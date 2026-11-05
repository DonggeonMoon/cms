package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationFlowTest extends ApiTestSupport {
    @Test
    void joiningSendsAWelcomeNotificationThatCanBeReadAndMarkedRead() throws Exception {
        MockHttpSession member = member("newcomer");

        getJson("/api/members/me/notifications/unread-count", member).andExpect(jsonPath("$").value(1));
        JsonNode list = body(getJson("/api/members/me/notifications", member));
        assertThat(list.get("content").get(0).get("title").asText()).isEqualTo("가입을 환영합니다");
        long notificationId = list.get("content").get(0).get("id").asLong();

        postJson("/api/members/me/notifications/" + notificationId + "/read", member, "").andExpect(status().isNoContent());
        getJson("/api/members/me/notifications/unread-count", member).andExpect(jsonPath("$").value(0));
    }

    @Test
    void payingAnOrderSendsAPaymentCompletedNotification() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"머그컵","price":10000,"availableQuantity":5,"isEnabled":true}
                """));

        MockHttpSession buyer = member("buyer");
        long orderId = createdId(postJson("/api/orders", buyer, """
                {"productId":%d,"quantity":1,"receiverName":"구매자","phoneNumber":"010-1234-5678","address":"서울"}
                """.formatted(productId)));
        postJson("/api/payments", buyer, """
                {"targetType":"ORDER","targetId":%d,"amount":10000,"method":"CARD"}
                """.formatted(orderId)).andExpect(status().isOk());

        JsonNode list = body(getJson("/api/members/me/notifications", buyer));
        assertThat(list.get("content").get(0).get("title").asText()).isEqualTo("결제가 완료되었습니다");
        assertThat(list.get("content").get(0).get("link").asText()).isEqualTo("/orders/" + orderId);
    }
}
