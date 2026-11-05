package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LicenseKeyFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    @Test
    void payingForProductAssignsAnAvailableKeyAndStockRunsOut() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"게임 라이선스","price":30000,"requiresShipping":false,"isEnabled":true}
                """));
        postJson("/api/admin/license-keys/bulk", admin, """
                {"productId":%d,"codes":["AAA-111","BBB-222"]}
                """.formatted(productId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.added").value(2))
                .andExpect(jsonPath("$.duplicate").value(0));

        postJson("/api/admin/license-keys/bulk", admin, """
                {"productId":%d,"codes":["AAA-111","CCC-333"]}
                """.formatted(productId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.added").value(1))
                .andExpect(jsonPath("$.duplicate").value(1));

        getJson("/api/admin/license-keys/available-count?productId=" + productId, admin)
                .andExpect(jsonPath("$").value(3));

        MockHttpSession buyer1 = member("buyer1");
        long order1 = createdId(postJson("/api/orders", buyer1, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(productId, RECEIVER)));
        postJson("/api/payments", buyer1, """
                {"targetType":"ORDER","targetId":%d,"amount":30000,"method":"CARD"}
                """.formatted(order1)).andExpect(status().isOk());

        JsonNode mine1 = body(getJson("/api/license-keys/me", buyer1).andExpect(status().isOk()));
        assertThat(mine1.get("content").size()).isEqualTo(1);
        String assignedCode = mine1.get("content").get(0).get("code").asText();
        assertThat(assignedCode).isIn("AAA-111", "BBB-222", "CCC-333");

        getJson("/api/admin/license-keys/available-count?productId=" + productId, admin)
                .andExpect(jsonPath("$").value(2));

        MockHttpSession buyer2 = member("buyer2");
        getJson("/api/license-keys/me", buyer2).andExpect(jsonPath("$.content").isEmpty());

        long order2 = createdId(postJson("/api/orders", buyer2, """
                {"productId":%d,"quantity":2,%s}
                """.formatted(productId, RECEIVER)));
        postJson("/api/payments", buyer2, """
                {"targetType":"ORDER","targetId":%d,"amount":60000,"method":"CARD"}
                """.formatted(order2)).andExpect(status().isOk());

        getJson("/api/admin/license-keys/available-count?productId=" + productId, admin)
                .andExpect(jsonPath("$").value(0));
        JsonNode mine2 = body(getJson("/api/license-keys/me", buyer2));
        assertThat(mine2.get("content").size()).isEqualTo(2);

        MockHttpSession buyer3 = member("buyer3");
        long order3 = createdId(postJson("/api/orders", buyer3, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(productId, RECEIVER)));
        postJson("/api/payments", buyer3, """
                {"targetType":"ORDER","targetId":%d,"amount":30000,"method":"CARD"}
                """.formatted(order3)).andExpect(status().isOk());
        getJson("/api/license-keys/me", buyer3).andExpect(jsonPath("$.content").isEmpty());

        JsonNode list = body(getJson("/api/admin/license-keys?productId=" + productId, admin));
        assertThat(list.get("content").size()).isEqualTo(3);
        assertThat(list.get("content").get(0).get("orderId").isNull()).isFalse();
        long assignedId = list.get("content").get(0).get("id").asLong();
        deleteJson("/api/admin/license-keys/" + assignedId, admin, null).andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
    }
}
