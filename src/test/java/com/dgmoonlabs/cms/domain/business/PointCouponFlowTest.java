package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.domain.common.point.service.PointService;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PointCouponFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    @Autowired
    private PointService pointService;

    @Test
    void signupRewardsCouponAndPointsSettleOnPartialAndFullCancel() throws Exception {
        MockHttpSession admin = admin();
        putJson("/api/admin/points/policy", admin, """
                {"isEnabled":true,"earnRate":10,"expireDays":30,"minUsePoints":100,"maxUseRate":50,"signupPoints":3000}
                """).andExpect(status().isOk());
        long welcome = body(postJson("/api/admin/coupons", admin, """
                {"name":"가입 축하 10%","discountType":"RATE","discountValue":10,"maxDiscountAmount":5000,"minOrderAmount":10000,
                 "issueType":"SIGNUP","validDays":30}
                """)).get("id").asLong();
        long a = createdId(postJson("/api/admin/products", admin, "{\"name\":\"A\",\"price\":20000,\"requiresShipping\":false,\"isEnabled\":true}"));
        long b = createdId(postJson("/api/admin/products", admin, "{\"name\":\"B\",\"price\":10000,\"requiresShipping\":false,\"isEnabled\":true}"));

        MockHttpSession member = member("saver");
        getJson("/api/members/me/points", member).andExpect(jsonPath("$.balance").value(3000));
        JsonNode coupon = body(getJson("/api/members/me/coupons", member)).get(0);
        assertThat(coupon.get("couponId").asLong()).isEqualTo(welcome);
        long issueId = coupon.get("id").asLong();

        String lines = "[{\"productId\":%d,\"quantity\":1},{\"productId\":%d,\"quantity\":1}]".formatted(a, b);
        postJson("/api/orders/preview", member, "{\"lines\":%s,\"couponIssueId\":%d,\"usePoints\":99999}".formatted(lines, issueId))
                .andExpect(jsonPath("$.code").value("POINTS_EXCEEDED"));
        postJson("/api/orders/preview", member, "{\"lines\":%s,\"couponIssueId\":%d,\"usePoints\":2000}".formatted(lines, issueId))
                .andExpect(jsonPath("$.couponDiscount").value(3000))
                .andExpect(jsonPath("$.pointsUsed").value(2000))
                .andExpect(jsonPath("$.totalPrice").value(25000))
                .andExpect(jsonPath("$.maxUsablePoints").value(3000))
                .andExpect(jsonPath("$.pointsToEarn").value(2499))
                .andExpect(jsonPath("$.coupons[0].discount").value(3000));

        long cartA = body(postJson("/api/cart", member, "{\"productId\":%d,\"quantity\":1}".formatted(a))).get("items").get(0).get("id").asLong();
        long cartB = body(postJson("/api/cart", member, "{\"productId\":%d,\"quantity\":1}".formatted(b))).get("items").get(1).get("id").asLong();
        JsonNode order = body(postJson("/api/cart/checkout", member, """
                {"cartItemIds":[%d,%d],%s,"couponIssueId":%d,"usePoints":2000}
                """.formatted(cartA, cartB, RECEIVER, issueId))
                .andExpect(jsonPath("$.totalPrice").value(25000))
                .andExpect(jsonPath("$.items[0].couponDiscount").value(2000))
                .andExpect(jsonPath("$.items[0].pointsUsed").value(1333))
                .andExpect(jsonPath("$.items[1].pointsUsed").value(667)));
        long orderId = order.get("id").asLong();
        getJson("/api/members/me/points", member).andExpect(jsonPath("$.balance").value(1000));
        getJson("/api/members/me/coupons", member).andExpect(jsonPath("$[0].status").value("USED"));

        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":25000,"method":"CARD"}
                """.formatted(orderId)).andExpect(jsonPath("$.status").value("APPROVED"));
        getJson("/api/orders/" + orderId, member)
                .andExpect(jsonPath("$.pointsEarned").value(2499))
                .andExpect(jsonPath("$.items[0].pointsEarned").value(1666));
        getJson("/api/members/me/points", member).andExpect(jsonPath("$.balance").value(3499));

        long itemB = order.get("items").get(1).get("id").asLong();
        postJson("/api/orders/%d/items/%d/cancel".formatted(orderId, itemB), member, "")
                .andExpect(jsonPath("$.items[1].refundedAmount").value(8333))
                .andExpect(jsonPath("$.refundedAmount").value(8333));
        getJson("/api/members/me/points", member).andExpect(jsonPath("$.balance").value(3499 + 667 - 833));

        postJson("/api/orders/%d/cancel".formatted(orderId), member, "").andExpect(status().isNoContent());
        getJson("/api/members/me/points", member).andExpect(jsonPath("$.balance").value(3000));
        getJson("/api/members/me/coupons", member).andExpect(jsonPath("$[0].status").value("AVAILABLE"));
        getJson("/api/members/me/points/history", member).andExpect(jsonPath("$.content[0].type").value("REVOKE"));
    }

    @Test
    void downloadCouponsAreLimitedAndPointsExpire() throws Exception {
        MockHttpSession admin = admin();
        long limited = body(postJson("/api/admin/coupons", admin, """
                {"name":"선착순 1천원","discountType":"AMOUNT","discountValue":1000,"issueType":"DOWNLOAD","totalQuantity":1}
                """)).get("id").asLong();
        MockHttpSession first = member("fast");
        MockHttpSession second = member("slow");
        postJson("/api/coupons/%d/download".formatted(limited), first, "").andExpect(status().isOk());
        postJson("/api/coupons/%d/download".formatted(limited), first, "").andExpect(jsonPath("$.code").value("COUPON_SOLD_OUT"));
        postJson("/api/coupons/%d/download".formatted(limited), second, "").andExpect(jsonPath("$.code").value("COUPON_SOLD_OUT"));
        getJson("/api/coupons", second).andExpect(jsonPath("$[0].soldOut").value(true));

        long memberId = body(getJson("/api/members/me", second)).get("id").asLong();
        postJson("/api/admin/coupons/%d/issues".formatted(limited), admin, "{\"usernames\":[\"slow\",\"nobody\"]}")
                .andExpect(jsonPath("$.issued").value(0))
                .andExpect(jsonPath("$.skipped.length()").value(2));
        postJson("/api/admin/members/%d/points".formatted(memberId), admin, "{\"amount\":500,\"memo\":\"이벤트\"}")
                .andExpect(status().isNoContent());
        getJson("/api/members/me/points", second).andExpect(jsonPath("$.balance").value(500));
        assertThat(pointService.expireDue(LocalDateTime.now().plusYears(2))).isEqualTo(1);
        getJson("/api/members/me/points", second).andExpect(jsonPath("$.balance").value(0));
    }
}
