package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StockMovementFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    @Test
    void orderPaymentCancelAndAdminAdjustAllLeaveStockMovementHistory() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"재고상품","price":10000,"requiresShipping":false,"isEnabled":true,"availableQuantity":10}
                """));

        MockHttpSession buyer = member("buyer1");
        long orderId = createdId(postJson("/api/orders", buyer, """
                {"productId":%d,"quantity":3,%s}
                """.formatted(productId, RECEIVER)));

        JsonNode afterOrder = body(getJson("/api/admin/stock-movements?productId=" + productId, admin));
        assertThat(afterOrder.get("content").size()).isEqualTo(1);
        assertThat(afterOrder.get("content").get(0).get("type").asText()).isEqualTo("OUT");
        assertThat(afterOrder.get("content").get(0).get("quantity").asInt()).isEqualTo(3);
        assertThat(afterOrder.get("content").get(0).get("quantityAfter").asInt()).isEqualTo(7);
        assertThat(afterOrder.get("content").get(0).get("orderId").asLong()).isEqualTo(orderId);

        postJson("/api/orders/" + orderId + "/cancel", buyer, "").andExpect(status().isNoContent());

        JsonNode afterCancel = body(getJson("/api/admin/stock-movements?productId=" + productId, admin));
        assertThat(afterCancel.get("content").size()).isEqualTo(2);
        assertThat(afterCancel.get("content").get(0).get("type").asText()).isEqualTo("IN");
        assertThat(afterCancel.get("content").get(0).get("quantityAfter").asInt()).isEqualTo(10);

        postJson("/api/admin/stock-movements/adjust", admin, """
                {"productId":%d,"delta":-2,"note":"실사 보정"}
                """.formatted(productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ADJUST"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.quantityAfter").value(8));

        JsonNode afterAdjust = body(getJson("/api/admin/stock-movements?productId=" + productId, admin));
        assertThat(afterAdjust.get("content").size()).isEqualTo(3);
        assertThat(afterAdjust.get("content").get(0).get("note").asText()).isEqualTo("실사 보정");

        postJson("/api/admin/stock-movements/adjust", admin, """
                {"productId":%d,"delta":100,"note":"초과 출고 거부 확인용"}
                """.formatted(productId));
        postJson("/api/admin/stock-movements/adjust", admin, """
                {"productId":%d,"delta":-1000,"note":"재고 부족"}
                """.formatted(productId))
                .andExpect(jsonPath("$.code").value("OUT_OF_STOCK"));
    }
}
