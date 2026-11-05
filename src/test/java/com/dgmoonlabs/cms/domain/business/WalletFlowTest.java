package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WalletFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    private long payCard(final MockHttpSession member, final long productId, final int amount) throws Exception {
        long orderId = createdId(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(productId, RECEIVER)));
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":%d,"method":"CARD"}
                """.formatted(orderId, amount)).andExpect(status().isOk());
        return orderId;
    }

    @Test
    void giftCardChargesWalletAndCodeCanBeIssuedAndRedeemed() throws Exception {
        MockHttpSession admin = admin();
        long giftCard = createdId(postJson("/api/admin/products", admin, """
                {"name":"상품권 5만원","price":50000,"saleType":"GIFT_CARD","isEnabled":true}
                """));

        MockHttpSession buyer = member("buyer");
        payCard(buyer, giftCard, 50000);
        getJson("/api/wallet/balance", buyer).andExpect(status().isOk()).andExpect(jsonPath("$").value(50000));

        JsonNode code = body(postJson("/api/wallet/gift-codes", buyer, "{\"amount\":20000}").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNUSED")));
        getJson("/api/wallet/balance", buyer).andExpect(jsonPath("$").value(30000));
        getJson("/api/wallet/gift-codes", buyer).andExpect(jsonPath("$.content[0].code").value(code.get("code").asText()));

        MockHttpSession receiver = member("receiver");
        postJson("/api/wallet/redeem", receiver, "{\"code\":\"%s\"}".formatted(code.get("code").asText()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REDEEMED"));
        getJson("/api/wallet/balance", receiver).andExpect(jsonPath("$").value(20000));

        postJson("/api/wallet/redeem", receiver, "{\"code\":\"%s\"}".formatted(code.get("code").asText()))
                .andExpect(jsonPath("$.code").value("GIFT_CARD_ALREADY_USED"));
    }

    @Test
    void prepaidMethodPaysFromWalletAndRefundsOnCancel() throws Exception {
        MockHttpSession admin = admin();
        long giftCard = createdId(postJson("/api/admin/products", admin, """
                {"name":"상품권 5만원","price":50000,"saleType":"GIFT_CARD","isEnabled":true}
                """));
        long mug = createdId(postJson("/api/admin/products", admin, """
                {"name":"머그컵","price":15000,"saleType":"PURCHASE","requiresShipping":false,"isEnabled":true}
                """));
        long expensive = createdId(postJson("/api/admin/products", admin, """
                {"name":"비싼 상품","price":40000,"saleType":"PURCHASE","requiresShipping":false,"isEnabled":true}
                """));

        MockHttpSession member = member("payer");
        payCard(member, giftCard, 50000);
        getJson("/api/wallet/balance", member).andExpect(jsonPath("$").value(50000));

        long orderId = createdId(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(mug, RECEIVER)));
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":15000,"method":"PREPAID"}
                """.formatted(orderId)).andExpect(status().isOk());
        getJson("/api/wallet/balance", member).andExpect(jsonPath("$").value(35000));

        postJson("/api/orders/%d/cancel".formatted(orderId), member, "").andExpect(status().isNoContent());
        getJson("/api/wallet/balance", member).andExpect(jsonPath("$").value(50000));

        long order2 = createdId(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(mug, RECEIVER)));
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":15000,"method":"PREPAID"}
                """.formatted(order2)).andExpect(status().isOk());

        long order3 = createdId(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(expensive, RECEIVER)));
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":40000,"method":"PREPAID"}
                """.formatted(order3)).andExpect(jsonPath("$.code").value("PREPAID_BALANCE_EXCEEDED"));
    }
}
