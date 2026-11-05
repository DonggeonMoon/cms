package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.domain.business.subscription.service.SubscriptionService;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SubscriptionChangeFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    @Autowired
    private SubscriptionService subscriptionService;

    private long product(final MockHttpSession admin, final String name, final int price, final String cycle) throws Exception {
        return createdId(postJson("/api/admin/products", admin, """
                {"name":"%s","price":%d,"saleType":"SUBSCRIPTION","billingCycle":"%s","isEnabled":true}
                """.formatted(name, price, cycle)).andExpect(status().isCreated()));
    }

    @Test
    void planChangesApplyFromTheNextBillingAndPausingKeepsRemainingDays() throws Exception {
        MockHttpSession admin = admin();
        long basic = product(admin, "원두 200g", 9900, "MONTHLY");
        long premium = product(admin, "원두 1kg 연간", 99000, "YEARLY");
        long purchase = createdId(postJson("/api/admin/products", admin, """
                {"name":"머그컵","price":5000,"isEnabled":true}
                """));

        MockHttpSession member = member("planner");
        long orderId = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":2,%s}
                """.formatted(basic, RECEIVER))).get("id").asLong();
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":19800,"method":"CARD"}
                """.formatted(orderId)).andExpect(status().isOk());
        long id = body(getJson("/api/subscriptions/me", member)).get("content").get(0).get("id").asLong();
        LocalDate firstBilling = LocalDate.now().plusMonths(1);

        getJson("/api/subscriptions/%d/plans".formatted(id), member)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(premium));
        putJson("/api/subscriptions/%d/plan".formatted(id), member, "{\"productId\":%d}".formatted(purchase))
                .andExpect(jsonPath("$.code").value("INVALID_SUBSCRIPTION_PLAN"));
        putJson("/api/subscriptions/%d/plan".formatted(id), member, "{\"productId\":%d}".formatted(premium))
                .andExpect(jsonPath("$.pendingProductName").value("원두 1kg 연간"))
                .andExpect(jsonPath("$.pendingPrice").value(198000))
                .andExpect(jsonPath("$.price").value(19800));

        assertThat(subscriptionService.renewDue(firstBilling)).isEqualTo(1);
        getJson("/api/orders/me", member)
                .andExpect(jsonPath("$.content[0].productName").value("원두 1kg 연간"))
                .andExpect(jsonPath("$.content[0].totalPrice").value(198000));
        getJson("/api/subscriptions/me", member)
                .andExpect(jsonPath("$.content[0].productId").value(premium))
                .andExpect(jsonPath("$.content[0].billingCycle").value("YEARLY"))
                .andExpect(jsonPath("$.content[0].pendingProductId").doesNotExist())
                .andExpect(jsonPath("$.content[0].nextBillingDate").value(firstBilling.plusYears(1).toString()));

        LocalDate today = LocalDate.now();
        long remaining = ChronoUnit.DAYS.between(today, firstBilling.plusYears(1));
        postJson("/api/subscriptions/%d/pause".formatted(id), member, "{\"resumeOn\":\"%s\"}".formatted(today))
                .andExpect(jsonPath("$.code").value("INVALID_RESUME_DATE"));
        postJson("/api/subscriptions/%d/pause".formatted(id), member, "{\"resumeOn\":\"%s\"}".formatted(today.plusDays(10)))
                .andExpect(jsonPath("$.status").value("PAUSED"))
                .andExpect(jsonPath("$.nextBillingDate").doesNotExist());
        postJson("/api/subscriptions/%d/pause".formatted(id), member, "")
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

        assertThat(subscriptionService.renewDue(today.plusDays(9))).isZero();
        getJson("/api/subscriptions/me", member).andExpect(jsonPath("$.content[0].status").value("PAUSED"));
        subscriptionService.renewDue(today.plusDays(10));
        getJson("/api/subscriptions/me", member)
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].nextBillingDate").value(today.plusDays(10 + remaining).toString()));

        postJson("/api/admin/subscriptions/%d/pause".formatted(id), admin, "").andExpect(jsonPath("$.resumeOn").doesNotExist());
        postJson("/api/subscriptions/%d/resume".formatted(id), member, "").andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
