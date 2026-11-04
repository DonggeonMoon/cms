package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.domain.business.subscription.service.SubscriptionService;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SaleTypeFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\"";

    @Autowired
    private SubscriptionService subscriptionService;

    @Test
    void subscriptionStartsOnPaymentRenewsAndCanBeCanceled() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/products", admin, """
                {"name":"원두 정기배송","price":9900,"saleType":"SUBSCRIPTION","isEnabled":true}
                """).andExpect(jsonPath("$.code").value("BILLING_CYCLE_REQUIRED"));
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"원두 정기배송","price":9900,"saleType":"SUBSCRIPTION","billingCycle":"MONTHLY","isEnabled":true}
                """).andExpect(status().isCreated()));
        getJson("/api/products/" + productId, null).andExpect(jsonPath("$.billingCycle").value("MONTHLY"));

        MockHttpSession member = member("subscriber");
        long orderId = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s,"address":"서울"}
                """.formatted(productId, RECEIVER))).get("id").asLong();
        pay(member, orderId, 9900);

        JsonNode subscription = body(getJson("/api/subscriptions/me", member)).get("content").get(0);
        assertThat(subscription.get("status").asString()).isEqualTo("ACTIVE");
        assertThat(subscription.get("nextBillingDate").asString()).isEqualTo(LocalDate.now().plusMonths(1).toString());

        assertThat(subscriptionService.renewDue(LocalDate.now().plusMonths(1))).isEqualTo(1);
        getJson("/api/orders/me", member)
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].status").value("PAID"))
                .andExpect(jsonPath("$.content[0].subscriptionId").value(subscription.get("id").asLong()));

        postJson("/api/subscriptions/" + subscription.get("id").asLong() + "/cancel", member, "")
                .andExpect(jsonPath("$.status").value("CANCELED"));
        assertThat(subscriptionService.renewDue(LocalDate.now().plusYears(1))).isZero();
    }

    @Test
    void donationsTakeAnAmountAboveTheMinimumAndCountTowardTheGoal() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"유기견 보호소 후원","price":1000,"saleType":"DONATION","donationPresets":[5000,10000],
                 "goalAmount":100000,"isEnabled":true}
                """));
        MockHttpSession member = member("donor");
        postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,"amount":500,%s}
                """.formatted(productId, RECEIVER)).andExpect(jsonPath("$.code").value("DONATION_AMOUNT_TOO_LOW"));
        long orderId = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,"amount":7000,%s}
                """.formatted(productId, RECEIVER))).get("id").asLong();
        pay(member, orderId, 7000);

        getJson("/api/products/" + productId, null)
                .andExpect(jsonPath("$.donationPresets[1]").value(10000))
                .andExpect(jsonPath("$.donation.raisedAmount").value(7000))
                .andExpect(jsonPath("$.donation.supporterCount").value(1));
    }

    @Test
    void quotesArePricedByTheAdminAndPaidAfterAcceptance() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"사무실 인테리어","price":0,"saleType":"QUOTE","availableQuantity":3,"isEnabled":true}
                """));
        MockHttpSession member = member("asker");
        long orderId = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,"requestMessage":"30평 사무실입니다",%s}
                """.formatted(productId, RECEIVER))
                .andExpect(jsonPath("$.status").value("QUOTE_REQUESTED"))
                .andExpect(jsonPath("$.totalPrice").doesNotExist())).get("id").asLong();
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":1,"method":"CARD"}
                """.formatted(orderId)).andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

        postJson("/api/admin/orders/" + orderId + "/quote", admin, """
                {"amount":5000000,"message":"자재 포함 견적입니다"}
                """).andExpect(jsonPath("$.status").value("QUOTED"));
        postJson("/api/orders/" + orderId + "/accept-quote", member, "")
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.quoteMessage").value("자재 포함 견적입니다"));
        getJson("/api/products/" + productId, null).andExpect(jsonPath("$.availableQuantity").value(2));
        pay(member, orderId, 5000000);
        getJson("/api/orders/" + orderId, member).andExpect(jsonPath("$.status").value("PAID"));
    }

    private void pay(final MockHttpSession member, final long orderId, final long amount) throws Exception {
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":%d,"method":"CARD"}
                """.formatted(orderId, amount)).andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
