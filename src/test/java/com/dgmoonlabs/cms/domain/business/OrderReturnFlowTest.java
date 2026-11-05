package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderReturnFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    private long payAndDeliver(final MockHttpSession admin, final MockHttpSession member, final long productId,
                               final Long optionId) throws Exception {
        long orderId = createdId(postJson("/api/orders", member, """
                {"productId":%d%s,"quantity":1,%s}
                """.formatted(productId, optionId == null ? "" : ",\"productOptionId\":" + optionId, RECEIVER)));
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":10000,"method":"CARD"}
                """.formatted(orderId)).andExpect(status().isOk());
        putJson("/api/admin/orders/" + orderId + "/status", admin, "{\"status\":\"SHIPPING\"}").andExpect(status().isNoContent());
        putJson("/api/admin/orders/" + orderId + "/status", admin, "{\"status\":\"DELIVERED\"}").andExpect(status().isNoContent());
        return orderId;
    }

    @Test
    void returnRequestApprovedRefundsAndRestoresStock() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"머그컵","price":10000,"availableQuantity":5,"isEnabled":true}
                """));
        MockHttpSession member = member("returner");
        long orderId = payAndDeliver(admin, member, productId, null);
        getJson("/api/products/" + productId, null).andExpect(jsonPath("$.availableQuantity").value(4));

        postJson("/api/orders/" + orderId + "/return-requests", member, """
                {"type":"RETURN","reason":"마음이 바뀌었어요"}
                """).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REQUESTED"));

        postJson("/api/orders/" + orderId + "/return-requests", member, """
                {"type":"RETURN","reason":"또 신청"}
                """).andExpect(jsonPath("$.code").value("RETURN_ALREADY_REQUESTED"));

        long returnId = body(getJson("/api/admin/orders/return-requests", admin)
                .andExpect(jsonPath("$.content[0].orderId").value(orderId))).get("content").get(0).get("id").asLong();

        postJson("/api/admin/orders/return-requests/" + returnId + "/approve", admin, "")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));

        getJson("/api/orders/" + orderId, member).andExpect(jsonPath("$.status").value("CANCELED"));
        getJson("/api/products/" + productId, null).andExpect(jsonPath("$.availableQuantity").value(5));
    }

    @Test
    void exchangeRequestApprovedCreatesPaidReplacementOrder() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"셔츠","price":10000,"isEnabled":true,
                 "options":[{"name":"빨강","additionalPrice":0},{"name":"파랑","additionalPrice":0}]}
                """));
        JsonNode product = body(getJson("/api/products/" + productId, null));
        long red = product.get("options").get(0).get("id").asLong();
        long blue = product.get("options").get(1).get("id").asLong();

        MockHttpSession member = member("exchanger");
        long orderId = payAndDeliver(admin, member, productId, red);

        long returnId = body(postJson("/api/orders/" + orderId + "/return-requests", member, """
                {"type":"EXCHANGE","reason":"색이 안 맞아요","exchangeOptionId":%d}
                """.formatted(blue)).andExpect(status().isOk())).get("id").asLong();

        postJson("/api/admin/orders/return-requests/" + returnId + "/approve", admin, "")
                .andExpect(status().isOk());

        getJson("/api/orders/" + orderId, member).andExpect(jsonPath("$.status").value("CANCELED"));
        JsonNode myOrders = body(getJson("/api/orders/me", member));
        JsonNode exchanged = myOrders.get("content").get(0);
        assertThat(exchanged.get("id").asLong()).isNotEqualTo(orderId);
        assertThat(exchanged.get("status").asString()).isEqualTo("PAID");
        assertThat(exchanged.get("productOptionId").asLong()).isEqualTo(blue);
    }

    @Test
    void rejectedRequestKeepsOrderUntouched() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"머그컵","price":10000,"isEnabled":true}
                """));
        MockHttpSession member = member("rejected");
        long orderId = payAndDeliver(admin, member, productId, null);

        long returnId = body(postJson("/api/orders/" + orderId + "/return-requests", member, """
                {"type":"RETURN","reason":"변심"}
                """)).get("id").asLong();

        postJson("/api/admin/orders/return-requests/" + returnId + "/reject", admin, """
                {"adminMemo":"사용한 흔적이 있습니다"}
                """).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));

        getJson("/api/orders/" + orderId, member).andExpect(jsonPath("$.status").value("DELIVERED"));

        postJson("/api/orders/" + orderId + "/return-requests", member, """
                {"type":"RETURN","reason":"다시 신청"}
                """).andExpect(status().isOk());
    }
}
