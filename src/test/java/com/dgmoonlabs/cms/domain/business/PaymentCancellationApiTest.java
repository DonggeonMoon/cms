package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentCancellationApiTest extends ApiTestSupport {
    @Test
    void cancelingAPaymentFromTheAdminListAlsoCancelsItsOrder() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"키보드","price":10000,"availableQuantity":5,"isEnabled":true}
                """).andExpect(status().isCreated()));

        MockHttpSession buyer = member("kbbuyer");
        JsonNode order = body(postJson("/api/orders", buyer, """
                {"productId":%d,"quantity":1,"receiverName":"구매자","phoneNumber":"010-0000-0000","address":"서울"}
                """.formatted(productId)));
        long orderId = order.get("id").asLong();

        postJson("/api/payments", buyer, """
                {"targetType":"ORDER","targetId":%d,"amount":10000,"method":"CARD"}
                """.formatted(orderId)).andExpect(jsonPath("$.status").value("APPROVED"));
        getJson("/api/orders/" + orderId, buyer).andExpect(jsonPath("$.status").value("PAID"));

        JsonNode payments = body(getJson("/api/admin/payments", admin));
        long paymentId = payments.get("content").get(0).get("id").asLong();

        postJson("/api/admin/payments/" + paymentId + "/cancel", admin, "").andExpect(status().isNoContent());
        getJson("/api/orders/" + orderId, buyer).andExpect(jsonPath("$.status").value("CANCELED"));
        getJson("/api/admin/payments", admin).andExpect(jsonPath("$.content[0].status").value("CANCELED"));
        getJson("/api/products/" + productId, null).andExpect(jsonPath("$.availableQuantity").value(5));

        postJson("/api/admin/payments/" + paymentId + "/cancel", admin, "")
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
    }
}
