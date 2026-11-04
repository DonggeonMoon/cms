package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatusApiTest extends ApiTestSupport {
    @Test
    void adminsRenameBaseStatusesAndAddDetailStatusesUnderThem() throws Exception {
        MockHttpSession admin = admin();
        JsonNode statuses = body(getJson("/api/statuses/ORDER", null).andExpect(jsonPath("$.length()").value(10)));
        long paidId = -1;
        for (JsonNode status : statuses) {
            if (status.get("code").asString().equals("PAID")) {
                paidId = status.get("id").asLong();
            }
        }
        putJson("/api/admin/statuses/items/" + paidId, admin, "{\"label\":\"입금 확인\"}").andExpect(jsonPath("$.label").value("입금 확인"));
        deleteJson("/api/admin/statuses/items/" + paidId, admin, null).andExpect(jsonPath("$.code").value("SYSTEM_STATUS_UNDELETABLE"));
        String packing = body(postJson("/api/admin/statuses/ORDER", admin, """
                {"label":"포장 중","baseStatus":"PAID","sequence":1}
                """)).get("code").asString();

        long productId = createdId(postJson("/api/admin/products", admin, "{\"name\":\"컵\",\"price\":1000,\"isEnabled\":true}"));
        JsonNode order = body(postJson("/api/admin/orders?paid=false", admin, """
                {"productId":%d,"quantity":1,"receiverName":"구매자","phoneNumber":"010-1234-5678"}
                """.formatted(productId)));
        long orderId = order.get("id").asLong();
        putJson("/api/admin/orders/%d/custom-status".formatted(orderId), admin, "{\"code\":\"" + packing + "\"}")
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
        postJson("/api/admin/orders/%d/pay".formatted(orderId), admin, "").andExpect(status().isNoContent());
        putJson("/api/admin/orders/%d/custom-status".formatted(orderId), admin, "{\"code\":\"" + packing + "\"}")
                .andExpect(status().isNoContent());
        getJson("/api/admin/orders?keyword=" + order.get("orderNumber").asString(), admin)
                .andExpect(jsonPath("$.content[0].customStatus").value(packing));

        putJson("/api/admin/orders/%d/status".formatted(orderId), admin, "{\"status\":\"SHIPPING\"}").andExpect(status().isNoContent());
        getJson("/api/admin/orders?keyword=" + order.get("orderNumber").asString(), admin)
                .andExpect(jsonPath("$.content[0].customStatus").doesNotExist());
    }
}
