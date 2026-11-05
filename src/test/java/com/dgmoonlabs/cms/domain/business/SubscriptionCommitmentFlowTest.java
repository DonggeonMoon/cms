package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SubscriptionCommitmentFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    @Test
    void cancellingWithinTheCommitmentPeriodChargesAProratedPenalty() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/products", admin, """
                {"name":"헬스장 약정권","price":10000,"saleType":"SUBSCRIPTION","billingCycle":"MONTHLY",
                 "commitmentMonths":12,"isEnabled":true}
                """).andExpect(jsonPath("$.code").value("COMMITMENT_FEE_RATE_REQUIRED"));

        long gym = createdId(postJson("/api/admin/products", admin, """
                {"name":"헬스장 약정권","price":10000,"saleType":"SUBSCRIPTION","billingCycle":"MONTHLY",
                 "commitmentMonths":12,"earlyTerminationFeeRate":50,"isEnabled":true}
                """));

        MockHttpSession member = member("committer");
        long orderId = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(gym, RECEIVER))).get("id").asLong();
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":10000,"method":"CARD"}
                """.formatted(orderId)).andExpect(status().isOk());

        long subscriptionId = body(getJson("/api/subscriptions/me", member)
                .andExpect(jsonPath("$.content[0].commitmentEndDate").value(LocalDate.now().plusMonths(12).toString()))
                .andExpect(jsonPath("$.content[0].earlyTerminationFeeRate").value(50))
                .andExpect(jsonPath("$.content[0].earlyTerminationFee").value(60000))).get("content").get(0).get("id").asLong();

        postJson("/api/subscriptions/%d/cancel".formatted(subscriptionId), member, "")
                .andExpect(jsonPath("$.status").value("CANCELED"));

        getJson("/api/orders/me", member)
                .andExpect(jsonPath("$.content[0].productName").value("헬스장 약정권 약정 해지 위약금"))
                .andExpect(jsonPath("$.content[0].totalPrice").value(60000))
                .andExpect(jsonPath("$.content[0].status").value("PAID"));
    }

    @Test
    void cancellingAPlainSubscriptionWithNoCommitmentIsFree() throws Exception {
        MockHttpSession admin = admin();
        long coffee = createdId(postJson("/api/admin/products", admin, """
                {"name":"원두 구독","price":9900,"saleType":"SUBSCRIPTION","billingCycle":"MONTHLY","isEnabled":true}
                """));

        MockHttpSession member = member("freecommit");
        long orderId = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(coffee, RECEIVER))).get("id").asLong();
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":9900,"method":"CARD"}
                """.formatted(orderId)).andExpect(status().isOk());

        long subscriptionId = body(getJson("/api/subscriptions/me", member)
                .andExpect(jsonPath("$.content[0].commitmentEndDate").doesNotExist())
                .andExpect(jsonPath("$.content[0].earlyTerminationFee").value(0))).get("content").get(0).get("id").asLong();

        postJson("/api/subscriptions/%d/cancel".formatted(subscriptionId), member, "")
                .andExpect(jsonPath("$.status").value("CANCELED"));

        getJson("/api/orders/me", member)
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("원두 구독"));
    }
}
